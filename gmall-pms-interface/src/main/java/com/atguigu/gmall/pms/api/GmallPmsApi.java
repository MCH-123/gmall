package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {

    @PostMapping("pms/spu/page")
    ResponseVo<List<SpuEntity>> querySpusByPage(@RequestBody PageParamVo pageParamVo);

    @GetMapping("pms/spu/{id}")
    ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);

    @GetMapping("pms/sku/{id}")
    ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);


    @GetMapping("pms/sku/spu/{spuId}")
    ResponseVo<List<SkuEntity>> querySkusBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/category/{id}")
    ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    @GetMapping("pms/category/parent/{parentId}")
    ResponseVo<List<CategoryEntity>> queryCategoriesByPid(@PathVariable("parentId")Long pid);

    @GetMapping("pms/brand/{id}")
    ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    @GetMapping("pms/spuattrvalue/spu/{spuId}")
    ResponseVo<List<SpuAttrValueEntity>> querySearchAttrValueBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/skuattrvalue/sku/{skuId}")
    ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValueBySkuId(@PathVariable("skuId") Long skuId);

    @GetMapping("pms/spuattrvalue/search/attr/value/{cid}")
    ResponseVo<List<SpuAttrValueEntity>> querySearchAttrValuesByCidAndSpuId(
            @PathVariable("cid") Long cid,
            @RequestParam("spuId") Long spuId
    );

    @GetMapping("pms/skuattrvalue/search/attr/value/{cid}")
    ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValuesByCidAndSkuId(
            @PathVariable("cid") Long cid,
            @RequestParam("skuId") Long skuId
    );

    @GetMapping("pms/category/subs/{pid}")
    ResponseVo<List<CategoryEntity>> queryCategoriesWithSub(@PathVariable("pid")Long pid);

    @GetMapping("pms/spudesc/{spuId}")
    @ApiOperation("详情查询")
    ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/category/all/{cid3}")
    ResponseVo<List<CategoryEntity>> queryCategoriesByCid3(@PathVariable Long cid3);

    @GetMapping("pms/skuimages/sku/{skuId}")
    ResponseVo<List<SkuImagesEntity>> queryImagesBySkuId(@PathVariable Long skuId);

    @GetMapping("pms/skuattrvalue/spu/{spuId}")
    ResponseVo<List<SaleAttrValueVo>> querySkuAttrValuesBySpuId(@PathVariable Long spuId);

    @GetMapping("pms/skuattrvalue/spu/sku/{spuId}")
    public ResponseVo<String> querySkusJsonBySpuId(@PathVariable Long spuId);

    @GetMapping("pms/attrgroup/with/attr/value/{cid}")
    ResponseVo<List<ItemGroupVo>> queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(
            @PathVariable("cid")Long cid, @RequestParam("spuId")Long spuId, @RequestParam("skuId")Long skuId
    );

    @GetMapping("pms/attrgroup/withattrvalues")
    ResponseVo<List<ItemGroupVo>> queryGroupsBySpuIdAndCid(
            @RequestParam Long spuId,
            @RequestParam Long skuId,
            @RequestParam Long cid
    );
}