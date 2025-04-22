package com.wu.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisLimitManagerTest {


    @Resource
    private RedisLimitManager redisLimitManager;

    @Test
    void limit() {
        String userid = "1";
        for (int i = 0; i < 10; i++) {
            redisLimitManager.limit(userid);
            System.out.println("成功");
        }


    }
}