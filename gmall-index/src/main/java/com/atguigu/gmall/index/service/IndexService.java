package com.atguigu.gmall.index.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.config.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsFeign;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndexService {
    @Autowired
    private GmallPmsFeign gmallPmsFeign;
    @Autowired
    private StringRedisTemplate redisTemplate;
    public static final String KEY_PREFIX = "index:cates:";
    private static final String LOCK_PREFIX = "index:cates:lock:";

    /**
     * 查询一级菜单
     * @return
     */
    @GmallCache(prefix = KEY_PREFIX,timeout = 14400,random = 3600,lock = LOCK_PREFIX)
    public List<CategoryEntity> queryLvl1Categories() {
//        String cacheCategories = this.redisTemplate.opsForValue().get(KEY_PREFIX + 0);
//        if (StringUtils.isNotBlank(cacheCategories)) {
//            return JSON.parseArray(cacheCategories, CategoryEntity.class);
//        }
        ResponseVo<List<CategoryEntity>> listResponseVo = this.gmallPmsFeign.queryCategoriesByPid(0L);
//        this.redisTemplate.opsForValue().set(KEY_PREFIX+0,JSON.toJSONString(listResponseVo.getData()),30, TimeUnit.DAYS);
        return listResponseVo.getData();
    }

    /**
     * 查询二级及其子菜单
     * @param pid 一级菜单id
     * @return
     */
    @GmallCache(prefix = KEY_PREFIX,timeout = 14400,random = 3600,lock = LOCK_PREFIX)
    public List<CategoryEntity> queryLvl2CategoriesWithSub(Long pid) {
        //从缓存中获取
/*        String cacheCategories = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(cacheCategories)) {
            //如果缓存中存在 直接返回
            return JSON.parseArray(cacheCategories, CategoryEntity.class);
        }*/
        ResponseVo<List<CategoryEntity>> listResponseVo = this.gmallPmsFeign.queryCategoriesWithSub(pid);
        //存入缓存
//        this.redisTemplate.opsForValue().set(KEY_PREFIX+pid,JSON.toJSONString(listResponseVo.getData()),30, TimeUnit.DAYS);
        return listResponseVo.getData();
    }
}
