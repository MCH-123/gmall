package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.pms.vo.AttrValueVo;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    private AttrMapper attrMapper;
    @Autowired
    private SpuAttrValueMapper spuAttrValueMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Override

    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<GroupVo> queryByCid(Long catId) {

        //查询所有的分组
        List<AttrGroupEntity> attrGroupEntities = this.list(
                Wrappers.lambdaQuery(AttrGroupEntity.class)
                        .eq(AttrGroupEntity::getCategoryId, catId)
        );
        //查询每组下的规格参数
        return attrGroupEntities.stream().map(
                attrGroupEntity -> {
                    GroupVo groupVo = new GroupVo();
                    BeanUtils.copyProperties(attrGroupEntity, groupVo);
                    //查询规格参数
                    List<AttrEntity> attrEntities = this.attrMapper.selectList(
                            Wrappers.lambdaQuery(AttrEntity.class)
                                    .eq(AttrEntity::getGroupId, attrGroupEntity.getId())
                                    .eq(AttrEntity::getType, 1)
                    );
                    groupVo.setAttrEntities(attrEntities);
                    return groupVo;
                }
        ).collect(Collectors.toList());
    }

    @Override
    public List<ItemGroupVo> queryGroupsBySpuIdAndCid(Long spuId, Long skuId, Long cid) {
        //根据cid查询分组
        List<AttrGroupEntity> attrGroupEntities = this.list(Wrappers.lambdaQuery(AttrGroupEntity.class)
                .eq(AttrGroupEntity::getCategoryId, cid));
        if (CollectionUtils.isEmpty(attrGroupEntities)) {
            return null;
        }
        //遍历分组查询每个分组下的attr
        return attrGroupEntities.stream().map(group -> {
            ItemGroupVo itemGroupVo = new ItemGroupVo();
            itemGroupVo.setGroupId(group.getId());
            itemGroupVo.setGroupName(group.getName());
            List<AttrEntity> attrEntities = this.attrMapper.selectList(Wrappers.lambdaQuery(AttrEntity.class).eq(AttrEntity::getGroupId, group.getId()));
            if (!CollectionUtils.isEmpty(attrEntities)) {
                List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());
                //attrId结合spuId查询规格参数对应值
                List<SpuAttrValueEntity> spuAttrValueEntities = this.spuAttrValueMapper.selectList(Wrappers.lambdaQuery(SpuAttrValueEntity.class)
                        .eq(SpuAttrValueEntity::getSpuId, spuId)
                        .in(SpuAttrValueEntity::getAttrId, attrIds));
                //attrId结合skuId查询规格参数对应值
                List<SkuAttrValueEntity> skuAttrValueEntities = this.skuAttrValueMapper.selectList(Wrappers.lambdaQuery(SkuAttrValueEntity.class)
                        .eq(SkuAttrValueEntity::getSkuId, skuId)
                        .in(SkuAttrValueEntity::getAttrId, attrIds));
                ArrayList<AttrValueVo> attrValueVos = new ArrayList<>();
                if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {
                    List<AttrValueVo> spuAttrValueVos = spuAttrValueEntities.stream().map(attrValue -> {
                        AttrValueVo attrValueVo = new AttrValueVo();
                        BeanUtils.copyProperties(attrValue, attrValueVo);
                        return attrValueVo;
                    }).collect(Collectors.toList());
                    attrValueVos.addAll(spuAttrValueVos);
                }
                if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                    List<AttrValueVo> skuAttrValueVos = skuAttrValueEntities.stream().map(attrValue -> {
                        AttrValueVo attrValueVo = new AttrValueVo();
                        BeanUtils.copyProperties(attrValue, attrValueVo);
                        return attrValueVo;
                    }).collect(Collectors.toList());
                    attrValueVos.addAll(skuAttrValueVos);
                }
                itemGroupVo.setAttrValues(attrValueVos);
            }
            return itemGroupVo;
        }).collect(Collectors.toList());

    }

}