package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商品三级分类
 *
 * @author menchuanhe
 * @email 1379325968@qq.com
 * @date 2023-01-27 17:01:24
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<CategoryEntity> queryCategory(Long parentId);

    List<CategoryEntity> queryCategoriesByPid(Long pid);

    List<CategoryEntity> queryCategoriesWithSub(Long pid);

    List<CategoryEntity> queryCategoriesByCid3(Long cid3);
}

