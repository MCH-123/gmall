package com.atguigu.gmall.pms.controller;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.service.SkuService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * sku信息
 *
 * @author menchuanhe
 * @email 1379325968@qq.com
 * @date 2023-01-27 17:01:24
 */
@Api(tags = "sku信息 管理")
@RestController
@RequestMapping("pms/sku")
public class SkuController {

    @Autowired
    private SkuService skuService;

    @ApiOperation("根据spuId查询sku列表")
    @GetMapping("spu/{spuId}")
    public ResponseVo<List<SkuEntity>> list(@PathVariable Long spuId) {
        List<SkuEntity> skuEntities = this.skuService.list(
                Wrappers.lambdaQuery(SkuEntity.class).eq(SkuEntity::getSpuId, spuId)
        );
        return ResponseVo.ok(skuEntities);
    }
    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> querySkuByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = skuService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id){
		SkuEntity sku = skuService.getById(id);

        return ResponseVo.ok(sku);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody SkuEntity sku){
		skuService.save(sku);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody SkuEntity sku){
		skuService.updateById(sku);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		skuService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
