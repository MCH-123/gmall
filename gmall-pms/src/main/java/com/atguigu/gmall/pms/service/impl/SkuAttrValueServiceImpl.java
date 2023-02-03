package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.vo.AttrValueVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private AttrMapper attrMapper;

    @Autowired
    private SkuMapper skuMapper;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuAttrValueEntity> querySearchAttrValueBySkuId(Long skuId) {
        return this.skuAttrValueMapper.querySearchAttrValueBySkuId(skuId);

    }

    @Override
    public List<SkuAttrValueEntity> querySearchAttrValuesByCidAndSkuId(Long cid, Long skuId) {

        List<AttrEntity> attrEntities = this.attrMapper.selectList(Wrappers.lambdaQuery(AttrEntity.class)
                .eq(AttrEntity::getCategoryId, cid)
                .eq(AttrEntity::getSearchType, 1)
                .eq(AttrEntity::getType, 0));
        if (CollectionUtils.isEmpty(attrEntities)) {
            return null;
        }
        List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());
        return this.list(Wrappers.lambdaQuery(SkuAttrValueEntity.class)
                .eq(SkuAttrValueEntity::getSkuId, skuId)
                .in(SkuAttrValueEntity::getAttrId, attrIds));

    }

    @Override
    public List<SaleAttrValueVo> querySkuAttrValuesBySpuId(Long spuId) {
/*        //根据spu查询sku
        List<SkuEntity> skuEntities = this.skuMapper.selectList(Wrappers.lambdaQuery(SkuEntity.class)
                .eq(SkuEntity::getSpuId, spuId));
        if (CollectionUtils.isEmpty(skuEntities)) {
            return null;
        }
        //获取skuId
        List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());

        List<SkuAttrValueEntity> skuAttrValueEntities = this.list(Wrappers.lambdaQuery(SkuAttrValueEntity.class)
                .in(SkuAttrValueEntity::getSkuId, skuIds));
        if (CollectionUtils.isEmpty(skuAttrValueEntities)) {
            return null;
        }*/
        List<AttrValueVo> attrValueVos = skuAttrValueMapper.querySkuAttrValuesBySpuId(spuId);

        Map<Long, List<AttrValueVo>> map = attrValueVos.stream().collect(Collectors.groupingBy(AttrValueVo::getAttrId));

        ArrayList<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();
        map.forEach((attrId,attrs)->{
            SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
            saleAttrValueVo.setAttrId(attrId);
            saleAttrValueVo.setAttrName(attrs.get(0).getAttrName());
            Set<String> attrValues = attrs.stream().map(AttrValueVo::getAttrValue).collect(Collectors.toSet());
            saleAttrValueVo.setAttrValues(attrValues);
            saleAttrValueVos.add(saleAttrValueVo);
        });
        return saleAttrValueVos;

    }

    @Override
    public String querySkusJsonBySpuId(Long spuId) {

        List<Map<String,Object>> skus = this.skuAttrValueMapper.querySkusJsonBySpuId(spuId);
        Map<String, Long> map = skus.stream().
                collect(Collectors.toMap(sku -> sku.get("attr_values").toString(), sku -> (Long) sku.get("sku_id")));
        return JSON.toJSONString(map);
    }

}