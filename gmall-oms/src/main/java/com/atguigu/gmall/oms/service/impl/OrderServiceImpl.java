package com.atguigu.gmall.oms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.entity.OrderItemEntity;
import com.atguigu.gmall.oms.entity.OrderItemVo;
import com.atguigu.gmall.oms.entity.OrderSubmitVo;
import com.atguigu.gmall.oms.mapper.OrderItemMapper;
import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.atguigu.gmall.oms.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderEntity> implements OrderService {
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<OrderEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<OrderEntity>()
        );

        return new PageResultVo(page);
    }
    @Transactional
    @Override
    public OrderEntity saveOrder(OrderSubmitVo orderSubmitVo, Long userId) {
        //保存订单
        OrderEntity orderEntity = new OrderEntity();
        BeanUtils.copyProperties(orderSubmitVo,orderEntity);
        orderEntity.setOrderSn(orderSubmitVo.getOrderToken());
        orderEntity.setUserId(userId);
        orderEntity.setCreateTime(new Date());
        orderEntity.setTotalAmount(orderSubmitVo.getTotalPrice());
        orderEntity.setPayAmount(orderSubmitVo.getTotalPrice());
        orderEntity.setPayType(orderSubmitVo.getPayType());
        orderEntity.setStatus(0);
        orderEntity.setDeliveryCompany(orderSubmitVo.getDeliveryCompany());
        this.save(orderEntity);

        //保存订单详情
        List<OrderItemVo> orderItems = orderSubmitVo.getItems();
        for (OrderItemVo orderItem : orderItems) {
            OrderItemEntity itemEntity = new OrderItemEntity();
            //订单信息
            itemEntity.setOrderId(orderEntity.getId());
            itemEntity.setOrderSn(orderEntity.getOrderSn());
            //设置sku信息
            itemEntity.setSkuId(orderItem.getSkuId());
            itemEntity.setSkuName(orderItem.getTitle());
            itemEntity.setSkuPrice(orderItem.getPrice());
            itemEntity.setSkuQuantity(orderItem.getCount());
            this.orderItemMapper.insert(itemEntity);
        }
        //发送延时消息 定时关单
        this.rabbitTemplate.convertAndSend("ORDER.EXCHANGE","order.create",orderSubmitVo.getOrderToken());
        return orderEntity;
    }

}