package org.inventorysystem.authservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.inventorysystem.authservice.dto.AuthResponse;
import org.inventorysystem.authservice.dto.LoginRequest;
import org.inventorysystem.authservice.dto.RegisterRequest;
import org.inventorysystem.authservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public Mono<ResponseEntity<String>> register(@RequestBody RegisterRequest request) {
        log.info("Register attempt for user: {}", request.getUsername());
        return userService.register(request.getUsername(), request.getPassword(), request.getRole())
                .map(user -> ResponseEntity.ok("User registered successfully"));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        return userService.login(request.getUsername(), request.getPassword());
    }

}

