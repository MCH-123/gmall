package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service("spuAttrValueService")
public class SpuAttrValueServiceImpl extends ServiceImpl<SpuAttrValueMapper, SpuAttrValueEntity> implements SpuAttrValueService {
    @Autowired
    private SpuAttrValueMapper spuAttrValueMapper;
    @Autowired
    private AttrMapper attrMapper;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SpuAttrValueEntity> querySearchAttrValueBySpuId(Long spuId) {

        return this.spuAttrValueMapper.querySearchAttrValueBySpuId(spuId);
    }

    @Override
    public List<SpuAttrValueEntity> querySearchAttrValuesByCidAndSpuId(Long cid, Long spuId) {
        //根据分类id查询出销售类型的检索属性
        List<AttrEntity> attrEntities = this.attrMapper.selectList(Wrappers.lambdaQuery(AttrEntity.class)
                .eq(AttrEntity::getCategoryId, cid).eq(AttrEntity::getSearchType, 1)
                .eq(AttrEntity::getType, 1));
        if (CollectionUtils.isEmpty(attrEntities)) {
            return null;
        }
        //规格参数id集合
        List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());
        //根据skuId查询销售类型的检索属性和值
        return this.list(Wrappers.lambdaQuery(SpuAttrValueEntity.class)
                .eq(SpuAttrValueEntity::getSpuId, spuId)
                .in(SpuAttrValueEntity::getAttrId, attrIds));

    }

}