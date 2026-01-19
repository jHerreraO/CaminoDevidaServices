package com.revoktek.services.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revoktek.services.model.User;
import com.revoktek.services.model.dto.LoginDTO;
import com.revoktek.services.service.UserService;
import com.revoktek.services.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import java.io.IOException;

import static org.springframework.web.multipart.support.MultipartResolutionDelegate.isMultipartRequest;

@RequiredArgsConstructor
@Log4j2
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final SpringValidatorAdapter validatorAdapter;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        if (isMultipartRequest(request)) {
            StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
            MultipartHttpServletRequest multipartRequest = resolver.resolveMultipart(request);
            String username = multipartRequest.getParameter("username");
            String password = multipartRequest.getParameter("password");

            if (username == null || password == null ) {
                throw new AuthenticationServiceException("Username or password parameter is missing");
            }

            // Create and populate the LoginDTO object
            LoginDTO credentials = new LoginDTO();
            credentials.setUsername(username);
            credentials.setPassword(password);

            // Validation block
            validateParams(credentials);
            UsernamePasswordAuthenticationToken authRequest = getUsernamePasswordAuthenticationToken(credentials);
            return authenticationManager.authenticate(authRequest);
        } else {
            throw new AuthenticationServiceException("Request is not multipart/form-data");
        }
    }


    private static UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(LoginDTO credentials) {
        UsernamePasswordAuthenticationToken authenticationToken;
        authenticationToken = new UsernamePasswordAuthenticationToken(credentials.getUsuario(), credentials.getPassword());
        authenticationToken.setDetails(credentials);
        return authenticationToken;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException {
        User usuario = (User) authResult.getPrincipal();
        LoginDTO credenciales = (LoginDTO) authResult.getDetails();


        String token = jwtUtil.getToken(usuario.getUsername(),usuario.getSimpleAuthorities());
        String refreshToken = jwtUtil.getRefreshToken(usuario.getUsername());
        response.addHeader("Authorization", "Bearer " + token);
        response.addHeader("refresh_token", "Bearer " + refreshToken);
        String rolesJson = new ObjectMapper().writeValueAsString(usuario.getSimpleAuthorities());
        response.addHeader("authorities", rolesJson);

        // Register logging event
        userService.logLogin(usuario, request);
    }
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.addHeader("error", failed.getMessage());
    }

    private void validateParams(LoginDTO credentials) {

        BeanPropertyBindingResult result = new BeanPropertyBindingResult(credentials, "credentials");
        validatorAdapter.validate(credentials, result);
        if (result.hasErrors()) {
            throw new AuthenticationServiceException("Credentials validation error");
        }
    }

}