package org.inventorysystem.authservice.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.inventorysystem.authservice.dto.AuthResponse;
import org.inventorysystem.authservice.entity.UserEntity;
import org.inventorysystem.authservice.exceptions.InvalidCredentialsException;
import org.inventorysystem.authservice.exceptions.UserAlreadyExistsException;
import org.inventorysystem.authservice.repository.UserRepository;
import org.inventorysystem.authservice.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public Mono<ResponseEntity<AuthResponse>> login(String username, String rawPassword) {
        return authenticate(username, rawPassword)
                .doOnSuccess(user -> log.info("Login successful for user: {}", user.getUsername()))
                .map(user -> {
                    String token = jwtService.generateToken(user);
                    return ResponseEntity.ok(new AuthResponse(token));
                });
    }

    public Mono<UserEntity> authenticate(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                .flatMap(user -> {
                    if (!BCrypt.verifyer().verify(rawPassword.toCharArray(), user.getPassword()).verified) {
                        return Mono.error(new InvalidCredentialsException());
                    }
                    return Mono.just(user);
                });
    }

    public Mono<Object> register(String username, String rawPassword, String role) {
        return userRepository.findByUsername(username)
                .flatMap(existing -> Mono.error(new UserAlreadyExistsException(username)))
                .switchIfEmpty(Mono.defer(() -> createAndSaveUser(username, rawPassword, role)));
    }

    private Mono<UserEntity> createAndSaveUser(String username, String rawPassword, String role) {
        String hashed = BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray());
        UserEntity newUser = UserEntity.builder()
                .username(username)
                .password(hashed)
                .role(role)
                .build();
        return userRepository.save(newUser)
                .doOnSuccess(saved -> log.info("User '{}' registered successfully", username));
    }

}
