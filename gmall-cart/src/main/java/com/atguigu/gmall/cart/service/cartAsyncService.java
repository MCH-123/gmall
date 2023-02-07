package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.bean.Cart;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class cartAsyncService {
    @Resource
    private CartMapper cartMapper;

    @Async
    public void updateCartByUserIdAndSkuId(String userId, Cart cart){
        cartMapper.update(cart, new UpdateWrapper<Cart>().eq("user_id", userId).eq("sku_id", cart.getSkuId()));
    }

    /**
     * 为了方便将来在异常处理器中获取异常用户信息
     * 所有异步方法的第一个参数统一为userId
     * @param userId
     * @param cart
     */
    @Async
    public void saveCart(String userId, Cart cart){
        this.cartMapper.insert(cart);
    }
}
