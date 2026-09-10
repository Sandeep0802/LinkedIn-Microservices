package com.sandeep.userservice.service;

import com.sandeep.userservice.dto.UpdateProfileRequest;
import com.sandeep.userservice.dto.UserResponse;
import com.sandeep.userservice.entity.Connection;
import com.sandeep.userservice.entity.ConnectionStatus;
import com.sandeep.userservice.entity.User;
import com.sandeep.userservice.event.ConnectionAcceptedEvent;
import com.sandeep.userservice.event.ConnectionRequestedEvent;
import com.sandeep.userservice.event.UserUpdatedEvent;
import com.sandeep.userservice.repository.ConnectionRepository;
import com.sandeep.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ConnectionRepository connectionRepository;
    private final S3Service s3Service;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String CONNECTION_REQUESTED_TOPIC = "connection.requested";
    private static final String CONNECTION_ACCEPTED_TOPIC = "connection.accepted";
    private static final String USER_UPDATED_TOPIC = "user.updated";


    public UserResponse getUserProfile(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found: " + userId));

        return mapToResponse(user);
    }



    public UserResponse updateProfile(String userId, UpdateProfileRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found: " + userId));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getHeadline() != null) {
            user.setHeadline(request.getHeadline());
        }

        if (request.getAbout() != null) {
            user.setAbout(request.getAbout());
        }

        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }

        if (request.getSkills() != null) {
            user.setSkills(request.getSkills());
        }

        User savedUser = userRepository.save(user);

        // Publish user.updated event
        UserUpdatedEvent event = new UserUpdatedEvent();

        event.setUserId(savedUser.getId());
        event.setFirstName(savedUser.getFirstName());
        event.setLastName(savedUser.getLastName());
        event.setHeadline(savedUser.getHeadline());
        event.setLocation(savedUser.getLocation());
        event.setSkills(savedUser.getSkills());
        event.setAbout(savedUser.getAbout());

        kafkaTemplate.send(
                USER_UPDATED_TOPIC,
                savedUser.getId(),
                event
        );

        log.info(
                "User updated and event published: {}",
                savedUser.getId()
        );

        return mapToResponse(savedUser);
    }


    public String sendConnectionRequest(String receiverId, String requesterId) {

        if (connectionRepository
                .existsByRequesterIdAndReceiverId(requesterId, receiverId)) {

            throw new RuntimeException("Connection already exists");
        }

        Connection connection = new Connection();

        connection.setRequesterId(requesterId);
        connection.setReceiverId(receiverId);
        connection.setStatus(ConnectionStatus.PENDING);

        connectionRepository.save(connection);

        // Publish connection.requested event
        ConnectionRequestedEvent event = new ConnectionRequestedEvent();

        event.setRequesterId(requesterId);
        event.setReceiverId(receiverId);

        kafkaTemplate.send(
                CONNECTION_REQUESTED_TOPIC,
                requesterId,
                event
        );

        log.info(
                "Connection request sent: {} -> {}",
                requesterId,
                receiverId
        );

        return "Connection Request Sent";
    }


    public String acceptConnectionRequest(String connectionId) {

        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Connection not found: " + connectionId
                        ));

        connection.setStatus(ConnectionStatus.CONNECTED);

        connectionRepository.save(connection);

        // Publish connection.accepted event
        ConnectionAcceptedEvent event = new ConnectionAcceptedEvent();

        event.setRequesterId(connection.getRequesterId());
        event.setReceiverId(connection.getReceiverId());

        kafkaTemplate.send(
                CONNECTION_ACCEPTED_TOPIC,
                connection.getRequesterId(),
                event
        );

        log.info("Connection accepted: {}", connectionId);

        return "Connection Accepted";
    }


    public List<UserResponse> getConnections(String userId) {

        // Get connections requested by this user
        // whose status is CONNECTED

        List<Connection> connections =
                connectionRepository.findByRequesterIdAndStatus(
                        userId,
                        ConnectionStatus.CONNECTED
                );

        return connections.stream()
                .map(e -> getUserProfile(e.getReceiverId()))
                .collect(Collectors.toList());
    }

    public List<Connection> getPendingConnections(String userId) {

         return connectionRepository.findByReceiverIdAndStatus(userId,ConnectionStatus.PENDING);
    }


    public  UserResponse uploadProfilePhoto(String userId, MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File cannot be empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found: " + userId));

        String photoUrl=s3Service.uploadFile(file,"profiles/"+userId+"/avatar");

         user.setProfilePhotoUrl(photoUrl);

         User savedUser=userRepository.save(user);

         log.info("Profile Photo Uploaded for User: {}",userId);
         return mapToResponse(savedUser);


    }

    public  UserResponse uploadCoverPhoto(String userId, MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File cannot be empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found: " + userId));

        String photoUrl=s3Service.uploadFile(file,"covers/"+userId+"/cover");

        user.setCoverPhotoUrl(photoUrl);

        User savedUser=userRepository.save(user);

        log.info("Cover Photo Uploaded for User: {}",userId);
        return mapToResponse(savedUser);


    }


    private UserResponse mapToResponse(User user) {

        UserResponse userResponse = new UserResponse();

        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setHeadline(user.getHeadline());
        userResponse.setAbout(user.getAbout());
        userResponse.setLocation(user.getLocation());
        userResponse.setProfilePhotoUrl(user.getProfilePhotoUrl());
        userResponse.setCoverPhotoUrl(user.getCoverPhotoUrl());
        userResponse.setRole(user.getRole());
        userResponse.setSkills(user.getSkills());
        userResponse.setCreatedAt(user.getCreatedAt());

        return userResponse;
    }



}