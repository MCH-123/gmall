package com.atguigu.gmall.ums.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.exception.UserException;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;


@Service("userService")
//@Repository
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        switch (type) {
            case 1:
                wrapper.eq(UserEntity::getUsername, data);
                break;
            case 2:
                wrapper.eq(UserEntity::getPhone, data);
                break;
            case 3:
                wrapper.eq(UserEntity::getEmail, data);
                break;
            default:
                return null;
        }
        return this.userMapper.selectCount(wrapper) == 0;

    }

    @Override
    public void register(UserEntity userEntity, String code) {
        // TODO: 2023/2/5 校验短信验证码
        //生成盐
        String salt = StringUtils.replace(UUID.randomUUID().toString(), "-", "");
        userEntity.setSalt(salt);
        //对密码加密
        String password = DigestUtils.md5Hex(salt + DigestUtils.md5Hex(userEntity.getPassword()));
        userEntity.setPassword(password);
        //设置其他信息
        userEntity.setCreateTime(new Date());
        userEntity.setLevelId(1L);
        userEntity.setStatus(1);
        userEntity.setIntegration(0);
        userEntity.setGrowth(0);
        userEntity.setNickname(userEntity.getUsername());
        //添加到数据库
        boolean b = this.save(userEntity);
        // TODO: 2023/2/5 删除缓存中的记录
    }

    @Override
    public UserEntity queryUser(String loginName, String password) {

        // 1.根据登录名查询用户信息（拿到盐）
        UserEntity userEntity = this.getOne(new QueryWrapper<UserEntity>()
                .eq("username", loginName)
                .or()
                .eq("phone", loginName)
                .or()
                .eq("email", loginName)
        );

        // 2.判断用户是否为空
        if (userEntity == null){
            throw new UserException("账户输入不合法！");
        }

        // 3.对密码加盐加密，并和数据库中的密码进行比较
        password = DigestUtils.md5Hex(password + userEntity.getSalt());
        if (!StringUtils.equals(userEntity.getPassword(), password)){
            throw new UserException("密码输入错误！");
        }

        // 4.返回用户信息
        return userEntity;
    }

}