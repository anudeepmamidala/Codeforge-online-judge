package com.example.codeforge.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisTestService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void pushJob(String code) {
    redisTemplate.opsForList().rightPush("submission_queue", code);
}
    public void test() {
        redisTemplate.opsForValue().set("test", "hello");
        System.out.println(redisTemplate.opsForValue().get("test"));
    }
}