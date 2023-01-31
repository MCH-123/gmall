package com.atguigu.gmall.search;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValueVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Test
    void contextLoads() {
        IndexOperations indexOps = this.restTemplate.indexOps(Goods.class);
        if (!indexOps.exists()){
            // 如果索引库不存在则创建新的索引库并声明映射
            indexOps.create();
            indexOps.putMapping(indexOps.createMapping());
        }


        Integer pageNum = 1;
        Integer pageSize = 100;
        do {
            // 1.分批查询spu
            ResponseVo<List<SpuEntity>> spuResponseVo = this.pmsClient.querySpusByPage(new PageParamVo(pageNum, pageSize, null));
            List<SpuEntity> spuEntities = spuResponseVo.getData();
            // 判断是否为空，则退出循环
            if (CollectionUtils.isEmpty(spuEntities)){
                return;
            }

            // 遍历spu集合，查询每一个spu下的sku，再把每一个sku转化成goods，导入到es
            spuEntities.forEach(spuEntity -> {
                // 根据spuId查询spu下的sku集合
                ResponseVo<List<SkuEntity>> skuResponseVo = this.pmsClient.querySkusBySpuId(spuEntity.getId());
                List<SkuEntity> skuEntities = skuResponseVo.getData();
                if (CollectionUtils.isEmpty(skuEntities)){
                    return; // 遍历下一个spu
                }

                // 如果spu下的sku不为空，此时查询品牌（同一个spu，品牌是一样的）
                ResponseVo<BrandEntity> brandEntityResponseVo = this.pmsClient.queryBrandById(spuEntity.getBrandId());
                BrandEntity brandEntity = brandEntityResponseVo.getData();

                ResponseVo<CategoryEntity> categoryEntityResponseVo = this.pmsClient.queryCategoryById(spuEntity.getCategoryId());
                CategoryEntity categoryEntity = categoryEntityResponseVo.getData();

                // 基本类型的检索属性和值 同一个spu，基本类型的检索属性和值都是一样的
                ResponseVo<List<SpuAttrValueEntity>> baseSearchAttrValueResposeVo = this.pmsClient.querySearchAttrValuesByCidAndSpuId(spuEntity.getCategoryId(), spuEntity.getId());
                List<SpuAttrValueEntity> spuAttrValueEntities = baseSearchAttrValueResposeVo.getData();

                // 把sku集合转化成goods集合，导入到es中
                this.restTemplate.save(skuEntities.stream().map(skuEntity -> {
                    Goods goods = new Goods();

                    // 设置sku基本信息
                    goods.setSkuId(skuEntity.getId());
                    goods.setTitle(skuEntity.getTitle());
                    goods.setSubtitle(skuEntity.getSubtitle());
                    goods.setPrice(skuEntity.getPrice().doubleValue());
                    goods.setDefaultImage(skuEntity.getDefaultImage());

                    // 设置创建时间
                    goods.setCreateTime(spuEntity.getCreateTime());

                    // 根据skuId查询库存
                    ResponseVo<List<WareSkuEntity>> wareSkuResponseVo = this.wmsClient.queryWareSkuBySkuId(skuEntity.getId());
                    List<WareSkuEntity> wareSkuEntities = wareSkuResponseVo.getData();
                    if (!CollectionUtils.isEmpty(wareSkuEntities)){
                        // 任何一个库存有货，都认为是有货
                        goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
                        goods.setSales(wareSkuEntities.stream().map(WareSkuEntity::getSales).reduce((a, b) -> a + b).get());
                    }

                    // 设置品牌相关字段
                    if (brandEntity != null) {
                        goods.setBrandId(brandEntity.getId());
                        goods.setBrandName(brandEntity.getName());
                        goods.setLogo(brandEntity.getLogo());
                    }

                    // 设置分类相关字段
                    if (categoryEntity != null) {
                        goods.setCategoryId(categoryEntity.getId());
                        goods.setCategoryName(categoryEntity.getName());
                    }

                    // 销售类型的检索属性和值
                    ResponseVo<List<SkuAttrValueEntity>> saleSearchAttrValueResponseVo = this.pmsClient.querySearchAttrValuesByCidAndSkuId(skuEntity.getCategoryId(), skuEntity.getId());
                    List<SkuAttrValueEntity> skuAttrValueEntities = saleSearchAttrValueResponseVo.getData();

                    List<SearchAttrValueVo> searchAttrs = new ArrayList<>();
                    // 把销售类型的检索属性和值 放入当前集合
                    if (!CollectionUtils.isEmpty(skuAttrValueEntities)){
                        searchAttrs.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                            SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                            BeanUtils.copyProperties(skuAttrValueEntity, searchAttrValueVo);
                            return searchAttrValueVo;
                        }).collect(Collectors.toList()));
                    }

                    // 把基本类型的检索属性和值 放入当前集合
                    if (!CollectionUtils.isEmpty(spuAttrValueEntities)){
                        searchAttrs.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                            SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                            BeanUtils.copyProperties(spuAttrValueEntity, searchAttrValueVo);
                            return searchAttrValueVo;
                        }).collect(Collectors.toList()));
                    }

                    goods.setSearchAttrs(searchAttrs);

                    return goods;
                }).collect(Collectors.toList()));
            });

            pageSize = spuEntities.size();
            pageNum++;
        } while (pageSize == 100);
    }
}
