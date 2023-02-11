package com.atguigu.gmall.wms.vo;

import lombok.Data;

@Data
public class SkuLockVo {
    private Long skuId; //锁定商品id
    private Integer count; //购买的数量
    private Boolean lock; //锁定状态
    private Long wareSkuId; //锁定仓库id
    private String orderToken; //订单号
}
