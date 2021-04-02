package com.nowcoder.community.service;

import com.nowcoder.community.utils.RedisKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LikeService {

    @Autowired
    RedisTemplate redisTemplate;

    // 点赞
    public void like(int userId, int entityType, int entityId,int entityUserId) {
     /*   String entityLikeKey = RedisKeyUtils.getEntityLikeKey(entityType, entityId);
        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        if (isMember) {
            redisTemplate.opsForSet().remove(entityLikeKey, userId);
        } else {
            redisTemplate.opsForSet().add(entityLikeKey, userId);
        }*/

       redisTemplate.execute(new SessionCallback() {

            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtils.getEntityLikeKey(entityType, entityId);
                boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
                String userLikeKey = RedisKeyUtils.getUserTotalLikeKey(entityUserId);
                redisOperations.multi();
                if (isMember) {
                    redisTemplate.opsForSet().remove(entityLikeKey, userId);
                    redisTemplate.opsForValue().decrement(userLikeKey);
                } else {
                    redisTemplate.opsForSet().add(entityLikeKey, userId);
                    redisTemplate.opsForValue().increment(userLikeKey);
                }

                return redisOperations.exec();
            }
        });


    }

    // 查询某实体点赞的数量
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtils.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    // 查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtils.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    //查询某个用户得到的赞数量
    public int findEntittyUserTotal(int entityUserId){
        String userTotalLikeKey = RedisKeyUtils.getUserTotalLikeKey(entityUserId);
        Integer i = (Integer) redisTemplate.opsForValue().get(userTotalLikeKey);
        return i != null?i.intValue():0;
    }

}
