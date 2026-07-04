package com.example.profile_service.client;

import org.springframework.stereotype.Component;

// Called when Auth Service circuit breaker is OPEN
// Returns true (optimistic) so profile creation is not blocked when Auth is down
// You can change this to false for stricter behaviour
@Component
public class AuthClientFallback implements AuthClient {

    @Override
    public boolean userExists(String userId) {
        // Fallback: Auth Service is down — allow profile creation optimistically
        // Log this in production so ops team knows CB is open
        return true;
    }
}
