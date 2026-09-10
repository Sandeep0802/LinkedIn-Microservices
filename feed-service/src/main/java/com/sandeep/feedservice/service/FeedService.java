package com.sandeep.feedservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedService {

    private final RedisTemplate<String,Object> redisTemplate;
    private static final String FEED_KEY_PREFIX="feed";

    public List<String> getFeed(String userId, int page, int size) {

         log.info("Getting feed for user: {}",userId);

         String feedKey=FEED_KEY_PREFIX+userId;

         //pagination Post range
         //db se kitni post fetch krni wo start and end batayega

        int start=page*size;
        int end=start+size-1;

        List<Object> postIds=redisTemplate.opsForList().range(feedKey,start,end);

         if(postIds==null || postIds.isEmpty()){
             log.info("Feed cache empty for user: {}",userId);
         }

         List<String> result=postIds.stream().map(Object::toString).toList();

         log.info("Returning {} posts for user: {}",result.size(),userId);

         return result;
    }


    //clear feed cache for user
    public void clearFeed(String userId) {

        String feedKey=FEED_KEY_PREFIX+userId;

        redisTemplate.delete(feedKey);

        log.info("Feed cache cleared for user: {}",userId);
    }
}
