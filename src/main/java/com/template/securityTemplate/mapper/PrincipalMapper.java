package com.template.securityTemplate.mapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class PrincipalMapper implements HandlerMethodArgumentResolver {
    private final EntityManager entityManager;
    private final Class<?> userType;
    private String userFieldName = "user";

    public PrincipalMapper(EntityManager entityManager, Class<?> userType) {
        this.entityManager = entityManager;
        this.userType = userType;
    }

    public PrincipalMapper(EntityManager entityManager, Class<?> userType, String userFieldName) {
        this.entityManager = entityManager;
        this.userType = userType;
        this.userFieldName = userFieldName;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer modelAndViewContainer,
                                  @NotNull NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        Principal principalTag = parameter.getParameterAnnotation(Principal.class);

        assert principalTag != null;
        final String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Class<?> modelClass = parameter.getParameterType();
        // return username only
        if (modelClass == String.class) {
            return principal;
        }

        Object user = userType.getConstructor().newInstance();
        // return user instance with persistence data. instance is created with mapper.
        System.out.println("Partes del query");

        if (principalTag.populate()) {
            TypedQuery<?> typedQueryUser = entityManager.createQuery(
                    "SELECT u " +
                            " FROM " + userType.getSimpleName() +
                            " u WHERE " + principalTag.usernameField() +
                            "= ?1", userType);
            typedQueryUser.setParameter(1, principal);

            if (modelClass == userType)
                return typedQueryUser.getSingleResult();

            TypedQuery<?> typedQueryModel = entityManager.createQuery(
                    "SELECT m " +
                            " FROM " + modelClass.getSimpleName() +
                            " m WHERE " + userFieldName +
                            " = ?1", modelClass);
            typedQueryModel.setParameter(1, typedQueryUser.getSingleResult());
            return typedQueryModel.getSingleResult();
        }

        Field usernameField = userType.getDeclaredField(principalTag.usernameField());
        usernameField.setAccessible(true);
        usernameField.set(user, principal);
        // return user instance with username set
        if (modelClass == userType)
            return user;

        // return model instance as last option
        Object model = modelClass.getConstructor().newInstance();
        Field userField = model.getClass().getDeclaredField(userFieldName);
        userField.setAccessible(true);
        usernameField.set(model, user);

        return model;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Principal.class);
    }
}
