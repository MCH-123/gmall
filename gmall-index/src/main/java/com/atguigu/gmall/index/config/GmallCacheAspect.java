package com.atguigu.gmall.index.config;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class GmallCacheAspect {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RBloomFilter bloomFilter;
    @Around("@annotation(com.atguigu.gmall.index.config.GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        //获取切点方法的签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取方法
        Method method = signature.getMethod();
        //获取方法上指定注解对象
        GmallCache annotation = method.getAnnotation(GmallCache.class);
        //获取注解中的前缀
        String prefix = annotation.prefix();
        //获取方法的参数
        Object[] args = joinPoint.getArgs();
        String param = StringUtils.join(args,",");
        //获取方法的返回值类型
        Class<?> returnType = method.getReturnType();
        String key = prefix + param;
        //判断缓存中是否存在
        String json = this.redisTemplate.opsForValue().get(key);
        //判断缓存中的数据是否为空
        if (StringUtils.isNotBlank(json)) {
            return JSON.parseObject(json, returnType);
        }
        //不存在 加锁
        String lock = annotation.lock();
        RLock rLock = this.redissonClient.getLock(lock + param);
        rLock.lock();
        //
        if (!bloomFilter.contains(key)) {
            return null;
        }

        String json2 = this.redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(json2)) {

            rLock.unlock();
            return JSON.parseObject(json2, returnType);
        }
        //执行方法目标
        Object result = joinPoint.proceed(joinPoint.getArgs());
        //放入缓存 释放分布锁
        int timeout = annotation.timeout();
        int random = annotation.random();
        this.redisTemplate.opsForValue().set(prefix+param,JSON.toJSONString(result),timeout+new Random().nextInt(random), TimeUnit.MINUTES);
        rLock.unlock();
        return result;
    }
}
