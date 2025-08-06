package org.inventorysystem.authservice.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.inventorysystem.authservice.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange); // Sin token, sigue sin autenticar
        }

        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            log.warn("Invalid JWT token");
            return chain.filter(exchange); // Token inválido
        }

        String username = jwtService.extractUsername(token);
        return userRepository.findByUsername(username)
                .map(user -> {
                    var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());
                    return new UsernamePasswordAuthenticationToken(user.getUsername(), null, Collections.singletonList(authority));
                })
                .flatMap(auth -> {
                    var context = new SecurityContextImpl(auth);
                    log.debug("Authenticated user: {}", auth.getPrincipal());
                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
                });
    }
}

