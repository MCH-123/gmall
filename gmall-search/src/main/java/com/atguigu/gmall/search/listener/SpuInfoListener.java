package com.atguigu.gmall.search.listener;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValueVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SpuInfoListener {
    @Autowired
    private ElasticsearchRestTemplate restTemplate;
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallWmsClient wmsClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "item_spu_queue",durable = "true"),
            exchange = @Exchange(value = "item_exchange",
            ignoreDeclarationExceptions = "true",
            type = ExchangeTypes.TOPIC),
            key = {"item.insert"}
    ))
    public void syncData(Long spuId, Channel channel, Message message) throws IOException {
        // 判断消息是否为空
        if (spuId == null) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 完成数据同步
        ResponseVo<SpuEntity> spuEntityResponseVo = this.pmsClient.querySpuById(spuId);
        SpuEntity spuEntity = spuEntityResponseVo.getData();
        if (spuEntity == null){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 根据spuId查询spu下的sku集合
        ResponseVo<List<SkuEntity>> skuResponseVo = this.pmsClient.querySkusBySpuId(spuId);
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
        ResponseVo<List<SpuAttrValueEntity>> baseSearchAttrValueResponseVo = this.pmsClient.querySearchAttrValuesByCidAndSpuId(spuEntity.getCategoryId(), spuEntity.getId());
        List<SpuAttrValueEntity> spuAttrValueEntities = baseSearchAttrValueResponseVo.getData();

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

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
