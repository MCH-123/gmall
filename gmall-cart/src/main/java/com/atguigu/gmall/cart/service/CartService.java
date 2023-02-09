package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.bean.Cart;
import com.atguigu.gmall.cart.bean.UserInfo;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallSmsClient smsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Resource
    private cartAsyncService cartAsyncService;
    private static final String KEY_PREFIX = "cart:info:";
    private static final String PRICE_PREFIX = "cart:price:";

    public void addCart(Cart cart) {
        // 1.获取登录信息
        String userId = getUserId();
        String key = KEY_PREFIX + userId;

        // 2.获取redis中该用户的购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        // 3.判断该用户的购物车信息是否已包含了该商品
        String skuId = cart.getSkuId().toString();
        BigDecimal count = cart.getCount(); // 用户添加购物的商品数量
        if (hashOps.hasKey(skuId)) {
            // 4.包含，更新数量
            String cartJson = hashOps.get(skuId).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(cart.getCount().add(count));
            this.cartAsyncService.updateCartByUserIdAndSkuId(userId, cart);
        } else {
            // 5.不包含，给该用户新增购物车记录 skuId count
            cart.setUserId(userId);
            // 根据skuId查询sku
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity != null) {
                cart.setTitle(skuEntity.getTitle());
                cart.setPrice(skuEntity.getPrice());
                cart.setDefaultImage(skuEntity.getDefaultImage());
            }

            // 根据skuId查询销售属性
            ResponseVo<List<SkuAttrValueEntity>> skuattrValueResponseVo = this.pmsClient.querySearchAttrValueBySkuId(cart.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = skuattrValueResponseVo.getData();
            cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));

            // 根据skuId查询营销信息
            ResponseVo<List<ItemSaleVo>> itemSaleVoResponseVo = this.smsClient.querySalesBySkuId(cart.getSkuId());
            List<ItemSaleVo> itemSaleVos = itemSaleVoResponseVo.getData();
            cart.setSales(JSON.toJSONString(itemSaleVos));

            // 根据skuId查询库存信息
            ResponseVo<List<WareSkuEntity>> listResponseVo = this.wmsClient.queryWareSkuBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = listResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }
            // 商品刚加入购物车时，默认为选中状态
            cart.setCheck(true);
            this.cartAsyncService.saveCart(userId, cart);
            //缓存实时价格
            this.redisTemplate.opsForValue().set(PRICE_PREFIX + skuId, skuEntity.getPrice().toString());
        }
        hashOps.put(skuId, JSON.toJSONString(cart));
    }

    public Cart queryCartBySkuId(Long skuId) {
        // 1.获取登录信息
        String userId = getUserId();
        String key = KEY_PREFIX + userId;

        // 2.获取redis中该用户的购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(skuId.toString())) {
            String cartJson = hashOps.get(skuId.toString()).toString();
            return JSON.parseObject(cartJson, Cart.class);
        }
        throw new RuntimeException("您的购物车中没有该商品记录！");
    }

    private String getUserId() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (userInfo.getUserId() != null) {
            // 如果用户的id不为空，说明该用户已登录，添加购物车应该以userId作为key
            return userInfo.getUserId().toString();
        }
        // 否则，说明用户未登录，以userKey作为key
        return userInfo.getUserKey();
    }

    public List<Cart> queryCarts() {

        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userKey = userInfo.getUserKey();
        // 1. 查询未登录的购物车
        String unloginKey = KEY_PREFIX + userKey;
        // 获取了未登录的购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(unloginKey);
        // 获取未登录购物车的json集合
        List<Object> cartJsons = hashOps.values();
        List<Cart> unloginCarts = null;
        // 反序列化为cart集合
        unloginCarts = this.getCarts(cartJsons);
        // 2. 判断是否登录，未登录直接返回
        Long userId = userInfo.getUserId();
        if (userId == null) {
            return unloginCarts;
        }

        // 3.合并购物车
        String loginKey = KEY_PREFIX + userId;
        // 获取了登录状态购物车操作对象
        BoundHashOperations<String, Object, Object> loginHashOps = this.redisTemplate.boundHashOps(loginKey);
        // 判断是否存在未登录的购物车，有则遍历未登录的购物车合并到已登录的购物车中去
        if (!CollectionUtils.isEmpty(unloginCarts)) {
            unloginCarts.forEach(cart -> {
                // 登录状态购物车已存在该商品，更新数量
                if (loginHashOps.hasKey(cart.getSkuId().toString())) {
                    // 未登录购物车当前商品的数量
                    BigDecimal count = cart.getCount();
                    // 获取登录状态的购物车并反序列化
                    String cartJson = loginHashOps.get(cart.getSkuId().toString()).toString();
                    cart = JSON.parseObject(cartJson, Cart.class);
                    // 更新登录状态的购物车
                    cart.setCount(cart.getCount().add(count));
                    this.cartAsyncService.updateCartByUserIdAndSkuId(userId.toString(), cart);
                } else {
                    // 登录状态购物车不包含该记录，新增
                    cart.setUserId(userId.toString()); // 用userId覆盖掉userKey
                    this.cartAsyncService.saveCart(userId.toString(), cart);
                }
                loginHashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
            });
            // 合并完未登录的购物车之后，要删除未登录的购物车
            this.cartAsyncService.deleteCartByUserId(userKey);
            this.redisTemplate.delete(unloginKey);
        }

        // 4.查询登录状态所有购物车信息，反序列化后返回
        List<Object> loginCartJsons = loginHashOps.values();
        return this.getCarts(loginCartJsons);
    }

    private List<Cart> getCarts(List<Object> cartJsons) {
        if (!CollectionUtils.isEmpty(cartJsons)) {
            return cartJsons.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                String currentPrice = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                if (currentPrice != null) {
                    cart.setCurrentPrice(new BigDecimal(currentPrice));
                }
                return cart;
            }).collect(Collectors.toList());
        }
        return null;
    }

    public void updateNum(Cart cart) {

        String userId = this.getUserId();
        String key = KEY_PREFIX + userId;
        //获取该用户的所有购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        //判断该用户购物车中是否包含该条信息
        if (hashOps.hasKey(cart.getSkuId().toString())) {
            try {
                BigDecimal count = cart.getCount();
                String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
                cart = JSON.parseObject(cartJson, Cart.class);
                cart.setCount(count);
                //更新到MySQL和Redis
                this.cartAsyncService.updateCartByUserIdAndSkuId(userId, cart);
                hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteCart(Long skuId) {
        String userId = this.getUserId();
        String key = KEY_PREFIX + userId;
        //获取该用户的所有购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        //判断用户购物车是否包含此信息
        if (hashOps.hasKey(skuId.toString())) {
            this.cartAsyncService.deleteCartByUserIdAndSkuId(userId, skuId);
            hashOps.delete(skuId.toString());
        }
    }
}
