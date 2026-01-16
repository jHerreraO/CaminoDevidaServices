package com.revoktek.services.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.revoktek.services.model.User;
import com.revoktek.services.model.enums.Authority;
import com.revoktek.services.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private final Algorithm signToken;
    private final Algorithm signRefreshToken;
    private final Algorithm externalSignToken;
    private final UserService userService;

    public JwtUtil(@Value("${key.secret}") String keySecret, @Value("${key.secretRefresh}") String keySecretRefresh,  @Value("${key.secretExternal}") String keySecretExternal, UserService userService) {
        this.signToken = Algorithm.HMAC512(keySecret);
        this.signRefreshToken = Algorithm.HMAC512(keySecretRefresh);
        this.externalSignToken = Algorithm.HMAC512(keySecretExternal);
        this.userService = userService;

    }

    public String getRefreshedToken(String refreshToken) {
        JWTVerifier verifier = JWT.require(signRefreshToken).build();
        DecodedJWT decodedJWT = verifier.verify(refreshToken);
        String username = decodedJWT.getSubject();
        // enable with flag security
        User user = userService.loadUserByUsername(username);
        return getToken(user.getUsername(), user.getSimpleAuthorities());
    }

    public String getToken(String username, List<Authority> authorities) {
        long now = System.currentTimeMillis();
        // 2 hour
        int tokenExpirationTime = 1000 * 60 * 60 * 8;
        return JWT.create()
                .withSubject(username)
                .withClaim("authorities", authorities.stream().map(Authority::name).toList())
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(now + tokenExpirationTime))
                .sign(signToken);
    }

    public String getRefreshToken(String username) {
        long now = System.currentTimeMillis();
        // 8 hours
        int refreshTokenExpirationTime = 1000 * 60 * 60 * 8;
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(now + refreshTokenExpirationTime))
                .sign(signRefreshToken);
    }

    public String getSubject(String jwt) {
        try {
            JWTVerifier verifier = JWT.require(signToken).build();
            // verify sign and expires time
            return verifier.verify(jwt).getSubject();
        } catch (Exception ignored) {
            return null;
        }
    }

    public VerifyTokenResult verifyToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(signToken).build();
            // verify sign and expires time
            DecodedJWT decodedJWT = verifier.verify(token);
            String username = decodedJWT.getSubject();
            Claim authoritiesClaim = decodedJWT.getClaim("authorities");
            Collection<GrantedAuthority> authorities = authoritiesClaim.asList(String.class).stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
            return new VerifyTokenResult(authenticationToken);
        } catch (Exception e) {
            return new VerifyTokenResult(e.getClass().getSimpleName(), e.getMessage());
        }
    }

    public VerifyTokenResult verifyExternalToken(String token) {
        try {
            log.info("🟡 Token received: {}", token);
            JWTVerifier verifier = JWT.require(externalSignToken).build();
            // verify sign and expires time
            DecodedJWT decodedJWT = verifier.verify(token);
            String username = decodedJWT.getSubject();
            Claim authoritiesClaim = decodedJWT.getClaim("authorities");
            Collection<GrantedAuthority> authorities = authoritiesClaim.asList(String.class).stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
            return new VerifyTokenResult(authenticationToken);
        } catch (Exception e) {
            log.error("Error verifying external token", e);
            return new VerifyTokenResult(e.getClass().getSimpleName(), e.getMessage());
        }
    }

    public boolean requireAdditionalTokenValidation(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // Lista de endpoints que requieren validación adicional
        List<String> endpointsRequiringAdditionalToken = Arrays.asList("/api/stats/", "/api/enrollTraveller/traveller/getAll","/api/logs/travellers","/api/logs/users","/api/alert/findall");
        return endpointsRequiringAdditionalToken.stream().anyMatch(uri::startsWith);
    }


    @Getter
    @Setter
    public static class VerifyTokenResult {
        private final UsernamePasswordAuthenticationToken token;
        private final String error;
        private final String exception;
        private final boolean errorPresent;

        public VerifyTokenResult(UsernamePasswordAuthenticationToken token) {
            this.token = token;
            this.errorPresent = false;
            this.error = null;
            this.exception = null;
        }

        public VerifyTokenResult(String exception, String error) {
            this.token = null;
            this.errorPresent = true;
            this.error = error;
            this.exception = exception;
        }
    }
}
