package com.atguigu.gmall.payment.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.payment.entity.PaymentInfoEntity;
import com.atguigu.gmall.payment.feign.GmallOmsClient;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.vo.PayAsyncVo;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

@Service
public class PaymentService {

    @Autowired
    private GmallOmsClient omsClient;
    @Resource
    private PaymentInfoMapper paymentInfoMapper;

    public OrderEntity queryOrderByOrderToken(String orderToken) {

        ResponseVo<OrderEntity> orderEntityResponseVo = this.omsClient.queryOrderByOrderSn(orderToken);
        return orderEntityResponseVo.getData();
    }

    public Long save(OrderEntity orderEntity, Integer payType){
        // 查看支付记录，是否已存在。
        PaymentInfoEntity paymentInfoEntity = this.paymentInfoMapper.selectOne(
                Wrappers.lambdaQuery(PaymentInfoEntity.class).eq(PaymentInfoEntity::getOutTradeNo, orderEntity.getOrderSn()));
        // 如果存在，直接结束
        if (paymentInfoEntity != null) {
            return paymentInfoEntity.getId();
        }
        // 否则，新增支付记录
        paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setOutTradeNo(orderEntity.getOrderSn());
        paymentInfoEntity.setPaymentType(payType);
        paymentInfoEntity.setSubject("谷粒商城支付平台");
        // paymentInfoEntity.setTotalAmount(orderEntity.getPayAmount());
        paymentInfoEntity.setTotalAmount(new BigDecimal("0.01"));
        paymentInfoEntity.setPaymentStatus(0);
        paymentInfoEntity.setCreateTime(new Date());
        this.paymentInfoMapper.insert(paymentInfoEntity);
        return paymentInfoEntity.getId();
    }

    /**
     * 根据id查询支付信息
     * @param id
     * @return
     */
    public PaymentInfoEntity queryPayMentById(Long id) {
        return this.paymentInfoMapper.selectById(id);
    }

    /**
     * 更新支付状态
     * @param payAsyncVo
     */
    public void paySuccess(PayAsyncVo payAsyncVo){
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setCallbackTime(new Date());
        paymentInfoEntity.setPaymentStatus(1);
        paymentInfoEntity.setCallbackContent(JSON.toJSONString(payAsyncVo));
        this.paymentInfoMapper.update(paymentInfoEntity,
                Wrappers.lambdaQuery(PaymentInfoEntity.class).eq(PaymentInfoEntity::getOutTradeNo, payAsyncVo.getOut_trade_no()));
    }
}
