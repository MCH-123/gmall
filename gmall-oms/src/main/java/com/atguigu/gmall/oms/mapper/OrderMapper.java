package com.atguigu.gmall.oms.mapper;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author menchuanhe
 * @email 1379325968@qq.com
 * @date 2023-01-28 18:23:28
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {
	
}
