package com.sandeep.notificationservice.controller;

import com.sandeep.notificationservice.entity.Notification;
import com.sandeep.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;


    @GetMapping("/{userId}")
    public List<Notification> getNotifications(
            @PathVariable String userId) {

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }


    @GetMapping("/{userId}/unread")
    public List<Notification> getUnreadNotifications(
            @PathVariable String userId) {

        return notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
    }


    @GetMapping("/{userId}/unread/count")
    public long getUnreadCount(
            @PathVariable String userId) {

        return notificationRepository
                .countByUserIdAndReadFalse(userId);
    }


    @PutMapping("/{notificationId}/read")
    public Notification markAsRead(
            @PathVariable String notificationId) {

        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification not found: "
                                                + notificationId
                                )
                        );

        notification.setRead(true);

        return notificationRepository.save(notification);
    }


    @PutMapping("/{userId}/read-all")
    public String markAllAsRead(
            @PathVariable String userId) {

        List<Notification> notifications =
                notificationRepository
                        .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                                userId
                        );

        notifications.forEach(
                notification ->
                        notification.setRead(true)
        );

        notificationRepository.saveAll(notifications);

        return "All notifications marked as read";
    }
}