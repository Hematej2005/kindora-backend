package com.kindora.kindora_backend.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;

public class AuthUtil {

    /**
     * Extract logged-in user's ID from SecurityContext.
     * Returns null when unauthenticated or when the principal doesn't expose an ID.
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) return null;

        Object principal = auth.getPrincipal();
        if (principal == null) return null;

        // case 1 — principal is a Long
        if (principal instanceof Long) return (Long) principal;

        // case 2 — principal.getId()
        try {
            Method m = principal.getClass().getMethod("getId");
            Object val = m.invoke(principal);
            if (val instanceof Number) return ((Number) val).longValue();
        } catch (Exception ignored) {}

        // case 3 — principal.getUserId()
        try {
            Method m = principal.getClass().getMethod("getUserId");
            Object val = m.invoke(principal);
            if (val instanceof Number) return ((Number) val).longValue();
        } catch (Exception ignored) {}

        // case 4 — authentication name might be numeric id
        try {
            String name = auth.getName();
            if (name != null) return Long.parseLong(name);
        } catch (Exception ignored) {}

        return null;
    }

    /**
     * Return the logged-in user's id or throw an IllegalStateException if not authenticated.
     * Use this when the caller expects a valid id and prefers a clear exception rather than null.
     */
    public static Long getLoggedUserId() {
        Long id = getCurrentUserId();
        if (id == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        return id;
    }
}
