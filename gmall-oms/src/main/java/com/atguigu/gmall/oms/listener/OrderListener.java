package com.atguigu.gmall.oms.listener;

import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
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
    private RabbitTemplate rabbitTemplate;
    @RabbitListener(queues = {"order-dead-queue"})
    public void closeOrder(String orderToken, Channel channel, Message message) throws IOException {
        //更新订单状态
        //执行关单操作 返回值为1,关单成功并解锁库存 返回值0,关单失败
        if (this.orderMapper.closeOrder(orderToken) == 1) {
            //解锁库存
            this.rabbitTemplate.convertAndSend("ORDER-EXCHANGE","stock.unlock",orderToken);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
