package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testStrings(){
        String redisKey = "test:count";
        redisTemplate.opsForValue().set(redisKey,1);

        redisTemplate.opsForValue().get(redisKey);
        redisTemplate.opsForValue().increment(redisKey);
        redisTemplate.opsForValue().decrement(redisKey);

        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    @Test
    public void testHash(){

        String redisKey = "test:user";
        redisTemplate.opsForHash().put(redisKey,"username","zhangsan");
        redisTemplate.opsForHash().put(redisKey,"password","123");

        System.out.println(redisTemplate.opsForHash().get(redisKey,"username"));
        System.out.println(redisTemplate.opsForHash().get(redisKey,"password"));


    }

    @Test
    public void testLists(){
        String redisKey = "test:ids";
       // redisTemplate.opsForList().leftPush(redisKey,101);
        // redisTemplate.opsForList().leftPush(redisKey,102);
        // redisTemplate.opsForList().leftPush(redisKey,103);
        // redisTemplate.opsForList().leftPush(redisKey,102);

        System.out.println(redisTemplate.opsForList().size(redisKey));

        System.out.println(redisTemplate.opsForList().index(redisKey, 0));

        System.out.println((redisTemplate.opsForList().range(redisKey,0,3)));


    }

    @Test
    public void testSets() {
        String redisKey = "test:teachers";

        redisTemplate.opsForSet().add(redisKey, "刘备", "关羽", "张飞", "赵云", "诸葛亮");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }







}
