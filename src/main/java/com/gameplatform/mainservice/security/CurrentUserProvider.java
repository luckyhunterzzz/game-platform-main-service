package com.gameplatform.mainservice.security;

import com.gameplatform.mainservice.exception.exceptions.InvalidAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserProvider {

    public UUID getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new InvalidAuthenticationException("Authentication is missing in SecurityContext");
        }

        String userId = authentication.getName();

        if (userId == null || userId.isBlank()) {
            throw new InvalidAuthenticationException("Authenticated user id is missing");
        }

        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new InvalidAuthenticationException("Authenticated user id is not a valid UUID: " + userId, e);
        }
    }
}
