package com.template.securityTemplate.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.template.securityTemplate.config.filter.JwtAuthenticationEntryPoint;
import com.template.securityTemplate.config.filter.JwtAuthenticationFilter;
import com.template.securityTemplate.config.filter.JwtAuthorizationFilter;
import com.template.securityTemplate.config.filter.LoggInterceptor;
import com.template.securityTemplate.service.UserService;
import com.template.securityTemplate.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Log4j2
@EnableMethodSecurity(prePostEnabled = false)
public class WebSecurityConfig {
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private static final String API_PUBLIC_PATTERN = "/api/public/**";

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // this bean replace manual injection of userService & password encoder
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity httpSecurity, AuthenticationManager authenticationManager, JwtUtil jwtUtil, LoggInterceptor loggInterceptor, ObjectMapper objectMapper, UserService userService, SpringValidatorAdapter validatorAdapter) throws Exception {

        JwtAuthenticationFilter jwtAuthenticationFilter =
                new JwtAuthenticationFilter(authenticationManager, jwtUtil, objectMapper, userService, validatorAdapter);
        JwtAuthorizationFilter jwtAuthorizationFilter = new JwtAuthorizationFilter(jwtUtil);
        jwtAuthenticationFilter.setFilterProcessesUrl("/api/login");

        // matching pattern spring security 6.1
        return httpSecurity
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .securityMatcher("/api/**")
                .authorizeHttpRequests(matcher ->
                        matcher
                                .anyRequest().authenticated()
                )
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handler -> handler.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .addFilter(jwtAuthenticationFilter)
                .addFilterAfter(loggInterceptor, JwtAuthenticationFilter.class)
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
