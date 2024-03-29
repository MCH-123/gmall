package com.atguigu.gmall.wms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
public interface GmallWmsApi {

    @GetMapping("wms/waresku/sku/{skuId}")
    ResponseVo<List<WareSkuEntity>> queryWareSkuBySkuId(@PathVariable("skuId") Long skuId);

    @PostMapping("wms/waresku/check/lock")
    ResponseVo<List<SkuLockVo>> checkAndLock(@RequestBody List<SkuLockVo> lockVos);
}

