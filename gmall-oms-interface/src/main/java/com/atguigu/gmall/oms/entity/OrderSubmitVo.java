package com.atguigu.gmall.oms.entity;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderSubmitVo {
    private String orderToken; //订单编号
    private UserAddressEntity address; //送货地址
    private Integer payType; //支付方式
    private String deliveryCompany; //物流方式
    private List<OrderItemVo> items; //送货清单
    private Integer bounds; //积分信息
    private BigDecimal totalPrice; //总价格
}
