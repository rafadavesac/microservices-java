package br.edu.atitus.gatewayapi.filters;

import br.edu.atitus.gatewayapi.components.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final List<String> PROTECTED_ROUTES = List.of("/ws/");

    private static final List<String> USER_HEADERS =
            List.of("X-User-Id", "X-User-Type", "X-User-Email");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        // Nunca confiar em cabeçalhos de identidade enviados pelo cliente
        ServerHttpRequest sanitizedRequest = request.mutate()
                .headers(headers -> USER_HEADERS.forEach(headers::remove))
                .build();

        String path = request.getURI().getPath();

        // Rota pública: segue sem identidade
        if (PROTECTED_ROUTES.stream().noneMatch(path::startsWith)) {
            return chain.filter(exchange.mutate().request(sanitizedRequest).build());
        }

        // Rota protegida: exige Bearer token válido
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            Claims claims = JwtUtil.validateToken(authHeader.substring(7));
            if (claims != null) {
                Long userId = claims.get("id", Long.class);
                Integer userType = claims.get("type", Integer.class);
                String email = claims.get("email", String.class);

                if (userId != null && userType != null && email != null) {
                    ServerHttpRequest authenticated = sanitizedRequest.mutate()
                            .headers(headers -> {
                                headers.set("X-User-Id", userId.toString());
                                headers.set("X-User-Type", userType.toString());
                                headers.set("X-User-Email", email);
                            }).build();
                    return chain.filter(exchange.mutate().request(authenticated).build());
                }
            }
        }

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
