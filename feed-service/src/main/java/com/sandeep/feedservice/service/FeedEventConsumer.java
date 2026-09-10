package com.sandeep.feedservice.service;

import com.sandeep.feedservice.client.UserServiceClient;
import com.sandeep.feedservice.event.PostCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedEventConsumer {

    private final UserServiceClient userServiceClient;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String FEED_KEY_PREFIX = "feed";

    @Value("${feed.max-size}")
    private int maxFeedSize;

    // Consume post.created event
    // When a user creates a post, immediately push that post
    // to all their connections' feeds

    @KafkaListener(topics = "post.created")
    public void consumePostCreatedEvent(PostCreatedEvent event) {

        try {

            String postId = event.getPostId();
            String authorId = event.getAuthorId();

            // Get author's connections from User Service
            List<Map<String, Object>> connections =
                    userServiceClient.getConnections(authorId);

            // Push post to every connection's feed
            for (Map<String, Object> connection : connections) {

                String connectionId =
                        (String) connection.get("id");

                String feedKey =
                        FEED_KEY_PREFIX + connectionId;

                redisTemplate.opsForList()
                        .leftPush(feedKey, postId);

                redisTemplate.opsForList()
                        .trim(feedKey, 0, maxFeedSize - 1);

                log.info(
                        "Post {} pushed to feed of user: {}",
                        postId,
                        connectionId
                );
            }

            // Also add post to author's own feed
            String authorFeedKey =
                    FEED_KEY_PREFIX + authorId;

            redisTemplate.opsForList()
                    .leftPush(authorFeedKey, postId);

            redisTemplate.opsForList()
                    .trim(
                            authorFeedKey,
                            0,
                            maxFeedSize - 1
                    );

            log.info(
                    "Post {} pushed to author's feed: {}",
                    postId,
                    authorId
            );

        } catch (Exception e) {

            log.error(
                    "Error in pushing post to feed: {}",
                    e.getMessage()
            );
        }
    }
}

//## Feed Updating using Kafka + Redis
//
//### Main idea
//
//When a user creates a post:
//
//        **Post Service → Kafka → Feed Service → User Service → Redis**
//
//        ### Step 1: Kafka
//
//Post Service publishes a `PostCreatedEvent` to:
//
//        `post.created`
//
//The event contains important information like:
//
//        * `postId`
//        * `authorId`
//        * `content`
//        * `imageUrl`
//
//Feed Service consumes it:
//
//        `PostCreatedEvent event`
//
//Then gets:
//
//        `postId = event.getPostId()`
//
//        `authorId = event.getAuthorId()`
//
//        ---
//
//        ### Step 2: Get connections
//
//Feed Service needs to know who should see the post.
//
//It calls User Service using Feign:
//
//        `getConnections(authorId)`
//
//Example:
//
//Author = `A`
//
//Connections:
//
//        `B, C, D`
//
//        ---
//
//        ### Step 3: Store post ID in Redis
//
//Each user has a Redis feed list:
//
//        `feedA`
//
//        `feedB`
//
//        `feedC`
//
//        `feedD`
//
//If A creates `post1`, then:
//
//        `feedA → [post1]`
//
//        `feedB → [post1]`
//
//        `feedC → [post1]`
//
//        `feedD → [post1]`
//
//We store **post IDs**, not the complete post.
//
//        ---
//
//        ### Why Redis?
//
//Redis is very fast and is used here to keep the user's feed ready.
//
//Instead of calculating the feed every time the user opens LinkedIn, we already prepare/update the feed when a post is created.
//
//        ---
//
//        ### Why `leftPush()`?
//
//        `leftPush()` adds the new post at the beginning.
//
//Example:
//
//        `feedB → [post5, post3, post1]`
//
//New post = `post6`
//
//After `leftPush()`:
//
//        `feedB → [post6, post5, post3, post1]`
//
//So newest posts stay at the front.
//
//---
//
//        ### Why `trim()`?
//
//We don't want Redis to store unlimited posts.
//
//If:
//
//        `feed.max-size = 100`
//
//then:
//
//        `trim(feedKey, 0, 99)`
//
//keeps only the latest **100 posts**.
//
//        ---
//
//        ### Why add the post to author's feed separately?
//
//The loop updates the feeds of the author's connections.
//
//Example:
//
//        `A → connections B, C`
//
//Loop updates:
//
//        `feedB`
//
//        `feedC`
//
//But A should also see their own post, so separately update:
//
//        `feedA`
//
//        ---
//
//        ### Complete Example
//
//A creates `post10`.
//
//        ```text
//A creates post10
//      ↓
//Post Service
//      ↓
//PostCreatedEvent
//      ↓
//Kafka: post.created
//      ↓
//Feed Service
//      ↓
//Get A's connections
//        ↓
//B, C
//      ↓
//Redis
//      ↓
//feedB → [post10]
//feedC → [post10]
//feedA → [post10]
//        ```
//
//        ### One-line memory trick
//
//**Kafka tells Feed Service about the new post → User Service tells who should receive it → Redis stores the post ID in their feeds.**
//
//This is basically **fan-out on write**: when a post is created, we immediately push its ID into the feeds of the relevant users.
