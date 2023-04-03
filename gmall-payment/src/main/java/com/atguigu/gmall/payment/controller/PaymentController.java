package com.atguigu.gmall.payment.controller;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.payment.service.PaymentService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Api("支付")
@Controller
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("pay.html")
    public String toPay(@RequestParam String orderToken, Model model) {
        OrderEntity orderEntity = this.paymentService.queryOrderByOrderToken(orderToken);
        model.addAttribute("orderEntity", orderEntity);
        return "pay";
    }
}
