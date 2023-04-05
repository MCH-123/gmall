package com.atguigu.gmall.oms.listener;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.entity.UserBoundVO;
import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.atguigu.gmall.oms.service.OrderService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OrderListener {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderService orderService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @RabbitListener(queues = {"order-dead-queue"})
    public void closeOrder(String orderToken, Channel channel, Message message) throws IOException {
        //更新订单状态
        //执行关单操作 返回值为1,关单成功并解锁库存 返回值0,关单失败
        if (this.orderMapper.closeOrder(orderToken) == 1) {
            //解锁库存
            this.rabbitTemplate.convertAndSend("ORDER.EXCHANGE","stock.unlock",orderToken);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ORDER-PAY-QUEUE", durable = "true"),
            exchange = @Exchange(value = "ORDER.EXCHANGE", ignoreDeclarationExceptions = "true"),
            key = {"order.pay"}
    ))
    public void successOrder(String orderToken, Channel channel, Message message) throws IOException {

        if (this.orderMapper.successOrder(orderToken) == 1){
            // 如果订单支付成功，真正的减库存
            this.rabbitTemplate.convertAndSend("ORDER.EXCHANGE", "stock.minus", orderToken);
            // 给用户添加积分信息
            OrderEntity orderEntity = this.orderService.getOne(Wrappers.lambdaQuery(OrderEntity.class).eq(OrderEntity::getOrderSn,orderToken));
            UserBoundVO userBoundVO = new UserBoundVO();
            userBoundVO.setUserId(orderEntity.getUserId());
            userBoundVO.setIntegration(orderEntity.getIntegration());
            userBoundVO.setGrowth(orderEntity.getGrowth());
            this.rabbitTemplate.convertAndSend("ORDER.EXCHANGE", "bound.plus", userBoundVO);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
}
