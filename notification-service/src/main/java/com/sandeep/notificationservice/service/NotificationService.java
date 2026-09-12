package com.sandeep.notificationservice.service;

import com.sandeep.notificationservice.entity.Notification;
import com.sandeep.notificationservice.event.*;
import com.sandeep.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;


    @KafkaListener(topics = "user.created")
    public void consumeUserCreatedEvent(UserCreatedEvent event) {

        try {

            String userId = event.getUserId();

            String firstName = event.getFirstName();

            sendNotification(
                    userId,
                    null,
                    "Welcome to LinkedIn!",
                    String.format(
                            "Welcome %s! Your account has been created. Start connecting with professionals.",
                            firstName
                    )
            );

        } catch (Exception e) {

            log.error(
                    "Error sending welcome notification: {}",
                    e.getMessage()
            );
        }
    }


    @KafkaListener(topics = "connection.requested")
    public void consumeConnectionRequestedEvent(
            ConnectionRequestedEvent event) {

        try {

            String receiverId = event.getReceiverId();

            String requesterId = event.getRequesterId();

            sendNotification(
                    receiverId,
                    requesterId,
                    "New Connection Request",
                    String.format(
                            "User %s wants to connect with you.",
                            requesterId
                    )
            );

        } catch (Exception e) {

            log.error(
                    "Error sending connection request notification: {}",
                    e.getMessage()
            );
        }
    }


    @KafkaListener(topics = "connection.accepted")
    public void consumeConnectionAcceptedEvent(
            ConnectionAcceptedEvent event) {

        try {

            String receiverId = event.getReceiverId();

            String requesterId = event.getRequesterId();

            // requester is the person who receives
            // "your request was accepted"
            sendNotification(
                    requesterId,
                    receiverId,
                    "Connection Accepted",
                    String.format(
                            "User %s accepted your connection request. You are now connected!",
                            receiverId
                    )
            );

        } catch (Exception e) {

            log.error(
                    "Error sending connection accept notification: {}",
                    e.getMessage()
            );
        }
    }


    @KafkaListener(topics = "post.liked")
    public void consumePostLikedEvent(
            PostLikedEvent event) {

        try {

            String authorId = event.getAuthorId();

            String userId = event.getUserId();

            String postId = event.getPostId();

            sendNotification(
                    authorId,
                    userId,
                    "Someone liked your post",
                    String.format(
                            "User %s liked your post %s.",
                            userId,
                            postId
                    )
            );

        } catch (Exception e) {

            log.error(
                    "Error sending like notification: {}",
                    e.getMessage()
            );
        }
    }


    @KafkaListener(topics = "post.commented")
    public void consumePostCommentedEvent(
            PostCommentedEvent event) {

        try {

            String postAuthorId = event.getPostAuthorId();

            String commenterId = event.getAuthorId();

            String postId = event.getPostId();

            sendNotification(
                    postAuthorId,
                    commenterId,
                    "New Comment on your post",
                    String.format(
                            "User %s commented on your post %s.",
                            commenterId,
                            postId
                    )
            );

        } catch (Exception e) {

            log.error(
                    "Error sending comment notification: {}",
                    e.getMessage()
            );
        }
    }


    private void sendNotification(
            String userId,
            String actorId,
            String title,
            String message) {

        Notification notification = new Notification();

        notification.setUserId(userId);

        notification.setActorId(actorId);

        notification.setTitle(title);

        notification.setMessage(message);

        notification.setRead(false);

        notificationRepository.save(notification);

        log.info(
                "Notification saved | receiver={} | actor={} | title={}",
                userId,
                actorId,
                title
        );
    }
}