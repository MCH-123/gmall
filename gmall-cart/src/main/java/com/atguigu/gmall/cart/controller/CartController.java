package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.bean.Cart;
import com.atguigu.gmall.cart.bean.UserInfo;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 添加购物车 重定向到购物车成功页
     * @param cart
     * @return
     */
    @GetMapping
    public String addCart(Cart cart) {
        if (cart == null || cart.getSkuId() == null) {
            throw new RuntimeException("没有选择添加到购物车的商品信息!");
        }
        this.cartService.addCart(cart);
        return "redirect:http://cart.gmall.com/addCart.html?skuId=" + cart.getSkuId();
    }

    /**
     * 跳转到成功页
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("addCart.html")
    public String addCart(@RequestParam("skuId") Long skuId, Model model) {
        Cart cart = this.cartService.queryCartBySkuId(skuId);
        model.addAttribute("cart", cart);
        return "addCart";
    }

    @GetMapping("cart.html")
    public String queryCarts(Model model) {
        List<Cart> carts = this.cartService.queryCarts();
        model.addAttribute("carts", carts);
        return "cart";
    }

    @PostMapping("updateNum")
    @ResponseBody
    public ResponseVo<Object> updateNum(@RequestBody Cart cart) {
        this.cartService.updateNum(cart);
        return ResponseVo.ok();
    }

    @DeleteMapping("deleteCart")
    @ResponseBody
    public ResponseVo<Object> deleteCart(@RequestParam("skuId") Long skuId) {
        this.cartService.deleteCart(skuId);
        return ResponseVo.ok();
    }

    @GetMapping("test")
    @ResponseBody
    public String test(){
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        System.out.println("userInfo = " + userInfo);
        return "hello cart!";
    }
}