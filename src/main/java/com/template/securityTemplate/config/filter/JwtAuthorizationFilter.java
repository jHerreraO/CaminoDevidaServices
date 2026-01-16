package com.template.securityTemplate.config.filter;


import com.template.securityTemplate.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Log4j2
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (request.getServletPath().equals("/api/login") || request.getServletPath().equals("/api/public/refreshToken") || jwtUtil.requireAdditionalTokenValidation(request) ) {
            chain.doFilter(request, response);
            return;
        }
        final String authorizationHeader = request.getHeader(AUTHORIZATION);
        String bearer = "Bearer ";
        if (authorizationHeader == null || !authorizationHeader.startsWith(bearer)) {
            chain.doFilter(request, response);
            return;
        }
        UsernamePasswordAuthenticationToken authenticationToken = null;
        JwtUtil.VerifyTokenResult verifyResult = jwtUtil.verifyToken(authorizationHeader.substring(bearer.length()));
        if (verifyResult.isErrorPresent()) {
            response.addHeader("error", verifyResult.getError());
        } else {
            authenticationToken = verifyResult.getToken();
        }
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        chain.doFilter(request, response);
    }

}