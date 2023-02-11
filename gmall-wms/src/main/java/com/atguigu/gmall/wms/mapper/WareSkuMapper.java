package com.atguigu.gmall.wms.mapper;

import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品库存
 * 
 * @author menchuanhe
 * @email 1379325968@qq.com
 * @date 2023-01-28 18:30:58
 */
@Mapper
public interface WareSkuMapper extends BaseMapper<WareSkuEntity> {

    List<WareSkuEntity> checkStock(Long skuId, Integer count);

    int lockStock(Long id, Integer count);

    int unlockStock(Long wareSkuId, Integer count);
}
