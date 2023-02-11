package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gmall.wms.service.WareSkuService;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuMapper, WareSkuEntity> implements WareSkuService {
    @Autowired
    private WareSkuMapper wareSkuMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String KEY_PREFIX = "store:lock:";

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<WareSkuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuLockVo> checkAndLock(List<SkuLockVo> lockVos) {

        if (CollectionUtils.isEmpty(lockVos)) {
            return null;
        }
        //每一个商品验库存并锁库存
        lockVos.forEach(this::checkLock);

        //一个锁定失败全部解锁
        List<SkuLockVo> successLockVo = lockVos.stream().filter(SkuLockVo::getLock).collect(Collectors.toList());
        List<SkuLockVo> errorLockVo = lockVos.stream().filter(skuLockVo -> !skuLockVo.getLock()).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(errorLockVo)) {
            successLockVo.forEach(lockVo -> this.wareSkuMapper.unlockStock(lockVo.getWareSkuId(), lockVo.getCount()));
            return lockVos;
        }
        //锁定库存信息保存到redis
        String orderToken = lockVos.get(0).getOrderToken();
        this.redisTemplate.opsForValue().set(KEY_PREFIX + orderToken, JSON.toJSONString(lockVos));
        //发送消息到延时队列 定时关单
        this.rabbitTemplate.convertAndSend("ORDER-EXCHANGE","stock.ttl",orderToken);
        return null;//全部锁定成功
    }

    private void checkLock(SkuLockVo lockVo) {
        RLock fairLock = this.redissonClient.getFairLock("lock:" + lockVo.getSkuId());
        fairLock.lock();
        try {
            //验库存
            List<WareSkuEntity> wareSkuEntities = this.wareSkuMapper.checkStock(lockVo.getSkuId(), lockVo.getCount());
            if (CollectionUtils.isEmpty(wareSkuEntities)) {
                lockVo.setLock(false); //锁定失败
                fairLock.unlock();
                return;
            }
            //锁库存
            if (this.wareSkuMapper.lockStock(wareSkuEntities.get(0).getId(), lockVo.getCount()) == 1) {
                lockVo.setLock(true);
                lockVo.setWareSkuId(wareSkuEntities.get(0).getId());
            } else {
                lockVo.setLock(false);
            }
        } finally {
            fairLock.unlock();
        }
    }

}