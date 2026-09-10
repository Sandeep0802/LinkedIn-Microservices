package com.sandeep.userservice.service;

import com.sandeep.userservice.dto.AuthResponse;
import com.sandeep.userservice.dto.LoginRequest;
import com.sandeep.userservice.dto.RegisterRequest;
import com.sandeep.userservice.entity.User;
import com.sandeep.userservice.entity.UserRole;
import com.sandeep.userservice.event.UserCreatedEvent;
import com.sandeep.userservice.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String USER_CREATED_TOPIC = "user.created";

     @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;


    public AuthResponse register(RegisterRequest request) {

        log.info("Registering user: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email Already Existes");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setHeadline(request.getHeadline());
        user.setLocation(request.getLocation());

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole(UserRole.NORMAL_USER);

        User savedUser = userRepository.save(user);

        log.info("User registered: {}", savedUser.getId());

        //  Publish user.created event
        //Search Service will consume this and index user

        UserCreatedEvent event = new UserCreatedEvent();

        event.setUserId(savedUser.getId());
        event.setFirstName(savedUser.getFirstName());
        event.setLastName(savedUser.getLastName());
        event.setEmail(savedUser.getEmail());
        event.setHeadline(savedUser.getHeadline());
        event.setLocation(savedUser.getLocation());

        kafkaTemplate.send(
                USER_CREATED_TOPIC,
                savedUser.getId(),
                event
        );

        log.info(
                "User created event published: {}",
                savedUser.getId()
        );

        String token = generateToken(savedUser.getId(), savedUser.getEmail());

        return buildAuthResponse(savedUser, token);

    }

    public AuthResponse login(LoginRequest request) {

        log.info("Login attempt: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException(
                        "User Not Found " + request.getEmail()
                ));


        //Bcrypt verify : compare raw password with stored hash

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            throw new RuntimeException("Invalid Credentials");
        }

        log.info("Login Successful: {}", user.getId());

        //Generate JWT Token

        String token = generateToken(user.getId(), user.getEmail());

        return buildAuthResponse(user, token);

    }


    public AuthResponse buildAuthResponse(User savedUser, String token) {

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(token);
        authResponse.setRefreshToken(
                generateRefreshToken(savedUser.getId())
        );
        authResponse.setUserId(savedUser.getId());
        authResponse.setEmail(savedUser.getEmail());
        authResponse.setFirstName(savedUser.getFirstName());
        authResponse.setLastName(savedUser.getLastName());

        return authResponse;

    }


    //generate access token
    public String generateToken(String userId, String email) {

        return Jwts.builder()
                .claim("userId", userId)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + jwtExpiration)
                )
                .signWith(getSigninKey(), SignatureAlgorithm.HS256)
                .compact();
    }

//generate refresh token

    //Used to get a new access token when it expires
//server validates and return a new access token
//client send refresh token token to /auth/refresh endpoint
    public String generateRefreshToken(String userId) {

        return Jwts.builder()
                .claim("userId", userId)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + refreshExpiration)
                )
                .signWith(getSigninKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public Key getSigninKey() {

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
