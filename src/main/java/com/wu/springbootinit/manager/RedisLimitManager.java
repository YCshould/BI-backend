package com.wu.springbootinit.manager;

import com.wu.springbootinit.common.ErrorCode;
import com.wu.springbootinit.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RedisLimitManager {

    /**
     *resource注解根据bean的名称自动注入
     */
    @Resource
    private RedissonClient redissonClient;

    public void limit(String key){
        // 创建限流器
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);

        // 设置限流规则：每秒最多允许 2 个操作
        rateLimiter.trySetRate(RateType.OVERALL, 2, 1, RateIntervalUnit.SECONDS);

        //请求令牌的数量
        boolean b = rateLimiter.tryAcquire(1);
        if(!b){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"未请求到令牌");
        }


    }
}
