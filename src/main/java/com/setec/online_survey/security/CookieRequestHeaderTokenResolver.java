package com.setec.online_survey.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

/**
 * Custom BearerTokenResolver that checks for a JWT in a specified HTTP cookie 
 * before falling back to the standard Authorization header.
 */
public class CookieRequestHeaderTokenResolver implements BearerTokenResolver {

    private final String cookieName;
    private BearerTokenResolver delegate = new DefaultBearerTokenResolver(); // Fallback resolver

    public CookieRequestHeaderTokenResolver(String cookieName) {
        this.cookieName = cookieName;
    }

    public void setDelegate(BearerTokenResolver delegate) {
        this.delegate = delegate;
    }

    @Override
    public String resolve(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        // Fallback: Check standard Authorization: Bearer header
        return this.delegate.resolve(request);
    }
}