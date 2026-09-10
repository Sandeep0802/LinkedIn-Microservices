package com.sandeep.userservice.controller;


import com.sandeep.userservice.dto.AuthResponse;
import com.sandeep.userservice.dto.LoginRequest;
import com.sandeep.userservice.dto.RegisterRequest;
import com.sandeep.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request){

        log.info("Register Request: {}",request.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));

    }

      @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request){

        log.info("Login Request: {}",request.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(authService.login(request));

    }





}
