package com.atguigu.gmall.oms.controller;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.entity.OrderSubmitVo;
import com.atguigu.gmall.oms.service.OrderService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单
 *
 * @author menchuanhe
 * @email 1379325968@qq.com
 * @date 2023-01-28 18:23:28
 */
@Api(tags = "订单 管理")
@RestController
@RequestMapping("oms/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @ApiOperation("根据订单编号查询订单")
    @GetMapping("token/{orderSn}")
    public ResponseVo<OrderEntity> queryOrderByOrderSn(@PathVariable String orderSn) {
        OrderEntity orderEntity = this.orderService.getOne(Wrappers.lambdaQuery(OrderEntity.class)
                .eq(OrderEntity::getOrderSn, orderSn));
        return ResponseVo.ok(orderEntity);
    }


    @ApiOperation("保存订单")
    @PostMapping("{userId}")
    public ResponseVo<OrderEntity> saveOrder(@RequestBody OrderSubmitVo orderSubmitVo,@PathVariable Long userId) {
        OrderEntity orderEntity = this.orderService.saveOrder(orderSubmitVo,userId);
        return ResponseVo.ok(orderEntity);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryOrderByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = orderService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<OrderEntity> queryOrderById(@PathVariable("id") Long id){
		OrderEntity order = orderService.getById(id);

        return ResponseVo.ok(order);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody OrderEntity order){
		orderService.save(order);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody OrderEntity order){
		orderService.updateById(order);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		orderService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
