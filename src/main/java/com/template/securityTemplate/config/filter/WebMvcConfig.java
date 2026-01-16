package com.template.securityTemplate.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.template.securityTemplate.mapper.DTOMapper;
import com.template.securityTemplate.mapper.PrincipalMapper;
import com.template.securityTemplate.model.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApplicationContext applicationContext;
    private final EntityManager entityManager;
    private final ModelMapper modelMapper;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        WebMvcConfigurer.super.addArgumentResolvers(resolvers);
        Set<HandlerMethodArgumentResolver> customResolvers = new HashSet<>();
        customResolvers.add(new PrincipalMapper(entityManager, User.class));

        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().applicationContext(applicationContext).build();
        resolvers.addAll(0, customResolvers);
        resolvers.add(new DTOMapper(objectMapper, modelMapper, entityManager));
    }

}


