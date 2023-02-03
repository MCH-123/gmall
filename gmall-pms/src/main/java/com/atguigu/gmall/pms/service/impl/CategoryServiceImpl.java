package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<CategoryEntity> queryCategory(Long parentId) {

        //构建查询条件 如果parentId为-1说明用户没有传入该字段,查询所有
        LambdaQueryWrapper<CategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (parentId != -1) {
            queryWrapper.eq(CategoryEntity::getParentId, parentId);
        }
        return this.categoryMapper.selectList(queryWrapper);
    }

    @Override
    public List<CategoryEntity> queryCategoriesByPid(Long pid) {
        LambdaQueryWrapper<CategoryEntity> wrapper = new LambdaQueryWrapper<>();
        if (pid!=-1) {
            wrapper.eq(CategoryEntity::getParentId, pid);
        }
        return this.list(wrapper);

    }

    @Override
    public List<CategoryEntity> queryCategoriesWithSub(Long pid) {

        return this.categoryMapper.queryCategoriesByPid(pid);
    }

    @Override
    public List<CategoryEntity> queryCategoriesByCid3(Long cid3) {

        //查询三级分类
        CategoryEntity categoryEntity3 = this.categoryMapper.selectById(cid3);
        //查询二级分类
        CategoryEntity categoryEntity2 = this.categoryMapper.selectById(categoryEntity3.getParentId());
        //查询一级分类
        CategoryEntity categoryEntity1 = this.categoryMapper.selectById(categoryEntity2.getParentId());
        return Arrays.asList(categoryEntity1, categoryEntity2, categoryEntity3);
    }

}