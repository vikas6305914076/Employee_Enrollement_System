package com.company.ems.common.util;

import com.company.ems.exception.AuthenticationException;
import com.company.ems.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("Authentication is required to access this resource");
        }
        if (!(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            throw new AuthenticationException("Unable to resolve the authenticated user");
        }
        return authenticatedUser;
    }

    public String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }
}
