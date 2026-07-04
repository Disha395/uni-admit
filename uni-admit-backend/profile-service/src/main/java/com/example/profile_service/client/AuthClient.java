package com.example.profile_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Calls AUTH-SERVICE via Eureka service discovery
// fallback = AuthClientFallback.class handles circuit breaker open state
@FeignClient(name = "auth-service", fallback = AuthClientFallback.class)
public interface AuthClient {

    // We'll add a /auth/users/{userId}/exists endpoint to Auth Service later
    // For now this validates the userId exists before creating a profile
    @GetMapping("/auth/users/{userId}/exists")
    boolean userExists(@PathVariable("userId") String userId);
}
