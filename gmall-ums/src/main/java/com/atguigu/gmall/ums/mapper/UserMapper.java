package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表
 * 
 * @author menchuanhe
 * @email 1379325968@qq.com
 * @date 2023-01-28 17:08:47
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
	
}
