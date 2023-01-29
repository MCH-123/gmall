package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    private AttrMapper attrMapper;
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
                    BeanUtils.copyProperties(attrGroupEntity,groupVo);
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

}