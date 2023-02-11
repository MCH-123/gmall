package com.atguigu.gmall.cart.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.bean.Cart;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class CartListener {
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Resource
    private CartMapper cartMapper;
    private static final String PRICE_PREFIX = "cart:price:";
    private static final String KEY_PREFIX = "cart:info:";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("CART.DELETE.QUEUE"),
            exchange = @Exchange(value = "ORDER.EXCHANGE",type = ExchangeTypes.TOPIC,ignoreDeclarationExceptions = "true"),
            key = {"cart.delete"}
    ))
    public void deleteCart(Map<String,Object> msg, Channel channel, Message message) throws IOException {
        try {
            //获取消息中的userId和skuIds
            Long userId = Long.valueOf(msg.get("userId").toString());
            List<String> skuIds = JSON.parseArray(msg.get("skuIds").toString(), String.class);

            //删除购物车中对应的记录
            BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
            hashOps.delete(skuIds);
            this.cartMapper.delete(Wrappers.lambdaQuery(Cart.class)
                    .eq(Cart::getUserId, userId)
                    .in(Cart::getSkuId, skuIds));
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            e.printStackTrace();
            if (message.getMessageProperties().getRedelivered()) {

                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } else {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
            }
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("CART.PRICE.QUEUE"),
            exchange = @Exchange(value = "PMS.SPU.EXCHANGE",type = ExchangeTypes.TOPIC,ignoreDeclarationExceptions = "true"),
            key = {"item.update"}
    ))
    public void syncPrice(Long spuId,Channel channel,Message message) throws IOException {
        if (spuId == null) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;
        }

        //根据spuId查询sku
        ResponseVo<List<SkuEntity>> skusResponseVo = this.pmsClient.querySkusBySpuId(spuId);
        List<SkuEntity> skuEntities = skusResponseVo.getData();
        if (CollectionUtils.isEmpty(skuEntities)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;
        }

        //遍历sku同步价格
        skuEntities.forEach(skuEntity -> this.redisTemplate.opsForValue().setIfPresent(PRICE_PREFIX + skuEntity.getId(), skuEntity.getPrice().toString()));

        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
