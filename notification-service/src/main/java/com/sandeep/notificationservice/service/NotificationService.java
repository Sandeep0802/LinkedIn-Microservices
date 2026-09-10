package com.sandeep.notificationservice.service;


import com.sandeep.notificationservice.event.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {


    @KafkaListener(topics = "user.created")
    public void consumeUserCreatedEvent(UserCreatedEvent event){

        try{
             String userId=event.getUserId();
             String firstName=event.getFirstName();

             sendNotification(userId,"Welcome to LinkedIn!",String.format(
                     "Welcome %s! Your account has been created "+"Start connecting with professionals.",
                     firstName
             ));
        }
        catch (Exception e){
            log.error("Error sending welcome notification: {}",e.getMessage());
        }
    }

    @KafkaListener(topics = "connection.requested")
    public void consumeConnectionRequestedEvent(ConnectionRequestedEvent event){

        try{
            String receiverId=event.getReceiverId();
            String requesterId=event.getRequesterId();

            sendNotification(receiverId,"New Connection Request",String.format(
                    "User %s want to connect with you.",
                    requesterId
            ));
        }
        catch (Exception e){
            log.error("Error sending connection request notification: {}",e.getMessage());
        }
    }

    @KafkaListener(topics = "connection.accepted")
    public void consumeConnectionAcceptedEvent(ConnectionAcceptedEvent event){

        try{
            String receiverId=event.getReceiverId();
            String requesterId=event.getRequesterId();

            sendNotification(requesterId,"Connection Accepted",String.format(
                    "User %s accepted your connection request "+"You are now connected!",
                            receiverId
            ));
        }
        catch (Exception e){
            log.error("Error sending connection accept notification: {}",e.getMessage());
        }
    }

    @KafkaListener(topics = "post.liked")
    public void consumePostLikedEvent(PostLikedEvent event){

        try{
            String authorId=event.getAuthorId();
            String userId=event.getUserId();
            String postId=event.getPostId();

            sendNotification(authorId,"Someone liked you post",String.format(
                    "User %s liked your post %s",
                    userId,postId
            ));
        }
        catch (Exception e){
            log.error("Error sending like notification: {}",e.getMessage());
        }
    }

    @KafkaListener(topics = "post.commented")
    public void consumePostCommentedEvent(PostCommentedEvent event){

        try{
            String postAuthorId=event.getPostAuthorId();
            String commenterId=event.getAuthorId();
            String postId=event.getPostId();

            sendNotification(postAuthorId,"New Comment on your post",String.format(
                    "User %s commented on your post %s",
                    commenterId,postId
            ));
        }
        catch (Exception e){
            log.error("Error sending comment notification: {}",e.getMessage());
        }
    }


    private void sendNotification(String userId, String title, String message) {

        log.info("=== NOTIFICATION ===");
        log.info("To User: {}", userId);
        log.info("Title: {}", title);
        log.info("Message: {}", message);
        log.info("====================");
    }

}
