package com.revoktek.services.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revoktek.services.rulesException.ModelNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.TypedQuery;
import jakarta.validation.constraints.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DTOMapper extends RequestResponseBodyMethodProcessor {
    private final ModelMapper modelMapper;
    private final EntityManager entityManager;

    public DTOMapper(ObjectMapper objectMapper, ModelMapper modelMapper, EntityManager entityManager) {
        super(Collections.singletonList(new MappingJackson2HttpMessageConverter(objectMapper)));
        this.entityManager = entityManager;
        this.modelMapper = modelMapper;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(DTO.class);
    }

    @Override
    protected void validateIfApplicable( WebDataBinder binder, MethodParameter parameter) {
        if (Objects.requireNonNull(parameter.getParameterAnnotation(DTO.class)).validate()) {
            binder.validate();
        }
    }
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Object dto = super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
        if (dto == null)
            throw new HttpMessageConversionException("Conversion content failed, make sure that content body is not empty.");
        DTO dtoTag = Objects.requireNonNull(parameter.getParameterAnnotation(DTO.class));
        Class<?> modelClass = parameter.getParameterType();
        Object id = getEntityId(dto);
        checkUniqueFields(dto, modelClass);
        if (id == null) {
            if (dtoTag.populate())
                return populateFields(dto, modelMapper.map(dto, modelClass));
            return modelMapper.map(dto, modelClass);
        }
        Object persistedObject = entityManager.find(modelClass, id);
        if (persistedObject == null)
            throw new ModelNotFoundException(modelClass, id);
        modelMapper.map(dto, persistedObject);
        return persistedObject;
    }

    private void checkUniqueFields(Object dto, Class<?> modelClass)
            throws IllegalArgumentException, IllegalAccessException, UniqueFieldException,
            NoSuchFieldException, SecurityException {
        for (Field field : dto.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            final String fieldName = field.getName();
            if (field.isAnnotationPresent(Model.class)) {
                Model modelTag = field.getAnnotation(Model.class);
                Field destinationField = modelClass.getDeclaredField(fieldName);
                destinationField.setAccessible(true);
                if (modelTag.isList()) {
                    for (Object item: (List<?>) field.get(dto)) {
                        checkUniqueFields(item, modelTag.typeList());
                    }
                }else {
                    checkUniqueFields(field.get(dto), destinationField.getType());
                }
            } else if (field.isAnnotationPresent(DTOUniqueField.class)) {
                DTOUniqueField uniqueTag = field.getAnnotation(DTOUniqueField.class);
                TypedQuery<Boolean> tq =
                        entityManager.createQuery(
                                "SELECT CASE WHEN (COUNT(*) > 0)  THEN true ELSE false END" +
                                        " FROM " + modelClass.getSimpleName() +
                                        " WHERE " + fieldName +
                                        "= ?1", Boolean.class);
                tq.setParameter(1, field.get(dto));
                if (tq.getSingleResult())
                    throw new UniqueFieldException(fieldName, field.get(dto), uniqueTag.message());
            }
        }
    }

    @Override
    protected Object readWithMessageConverters(@NotNull HttpInputMessage inputMessage, MethodParameter parameter,
                                               @NotNull Type targetType) throws IOException, HttpMediaTypeNotSupportedException, HttpMessageNotReadableException {
        for (Annotation ann : parameter.getParameterAnnotations()) {
            DTO dtoType = AnnotationUtils.getAnnotation(ann, DTO.class);
            if (dtoType != null) {
                return super.readWithMessageConverters(inputMessage, parameter, dtoType.value());
            }
        }
        throw new RuntimeException();
    }

    private Object getEntityId(Object dto) {
        for (Field field : dto.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                try {
                    field.setAccessible(true);
                    return field.get(dto);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    private <T> T populateFields(Object dto, T destination) throws ModelNotFoundException {
        for (Field field : dto.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(Model.class)) {
                continue;
            }
            try {
                Model modelTag = field.getAnnotation(Model.class);
                field.setAccessible(true);
                String modelFieldName = field.getName();
                Field destinationField = destination.getClass().getDeclaredField(modelFieldName);
                destinationField.setAccessible(true);
                Class<?> destinationFieldClass = destinationField.getType();
                if (modelTag.isList()) {
                    List<?> list = (List<?>) field.get(dto);
                    List<Object> listModel = new ArrayList<>();
                    if (modelTag.hasInsideModel()) {
                        Class<?> modelTagType = modelTag.typeList();
                        for (Object item : list) {
                            listModel.add(populateFields(item, modelMapper.map(item, modelTagType)));
                        }
                    } else {
                        for (Object item : list) {
                            Object persistedObject = getPersistenceFromDTO(item, destinationFieldClass);
                            listModel.add(persistedObject);
                        }
                    }
                    destinationField.set(destination, listModel);
                } else {
                    Object persistedObject = getPersistenceFromDTO(field.get(dto), destinationFieldClass);
                    destinationField.set(destination, persistedObject);
                    for (Field innerField : field.getClass().getDeclaredFields()) {
                        if (innerField.isAnnotationPresent(Model.class)) {
                            populateFields(persistedObject, destinationFieldClass);
                        }
                    }
                }
            } catch (IllegalAccessException | NoSuchFieldException | SecurityException e) {
                throw new RuntimeException(e);
            }
        }
        return destination;
    }

    private Object getPersistenceFromDTO(Object dto, Class<?> destinationClass) throws ModelNotFoundException {
        Object idValue = getEntityId(dto);
        Object persistenceObject = entityManager.find(destinationClass, idValue);
        if (persistenceObject == null)
            throw new ModelNotFoundException(destinationClass, idValue);
        return persistenceObject;
    }
}
