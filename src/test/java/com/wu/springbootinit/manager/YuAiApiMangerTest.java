package com.wu.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class YuAiApiMangerTest {

    @Resource
    private YuAiApiManger yuAiApiManger;

    @Test
    void toChat() {
        String result = yuAiApiManger.toChat(1651468516836098050L,"邓紫棋");
        System.out.println(result);
    }
}