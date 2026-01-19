package com.revoktek.services.service;


import com.revoktek.services.model.User;
import com.revoktek.services.model.enums.Authority;
import com.revoktek.services.repository.UserRepository;
import com.revoktek.services.rulesException.EnumInvalidArgumentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

/**
 * Servicio utilitario para operaciones comunes relacionadas con el usuario en sesión
 * y manipulación de enums personalizados.
 * <p>
 * Proporciona métodos para obtener el usuario actual en sesión, el nombre de usuario
 * en sesión, y para convertir cadenas a enums con validación.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class UtilService {

    private final UserRepository userRepository;

    /**
     * Obtiene el usuario actualmente autenticado en el contexto de seguridad.
     * <p>
     * Retorna el usuario más recientemente registrado que coincida con el nombre de usuario
     * autenticado en el SecurityContext.
     * </p>
     *
     * @return Usuario en sesión, o {@code null} si no hay autenticación o no se encuentra el usuario.
     */
    public User userInSession() {
        if (SecurityContextHolder.getContext().getAuthentication() == null)
            return null;
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findFirstByUsernameOrderByDateRegisterDesc(username);
        if (user == null) {
            log.warn("No se encontró usuario con username '{}' en la base de datos", username);
            return null;
        }
        return user;
    }

    /**
     * Obtiene el nombre de usuario (username) de la sesión actual.
     *
     * @return Nombre de usuario autenticado, o {@code null} si no hay autenticación.
     */
    public String userNameInSession() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return "";
        }

        Object principal = auth.getPrincipal();

        // Spring Security usa "anonymousUser" como string
        if (principal instanceof String && "anonymousUser".equals(principal)) {
            return "";
        }

        return principal.toString();
    }


    /**
     * Convierte un valor {@code String} en el enum {@link Authority}.
     * <p>
     * La conversión es case-insensitive y valida que el valor exista dentro del enum.
     * </p>
     *
     * @param value Cadena a convertir.
     * @return Valor del enum {@link com.revoktek.services.model.enums.Authority}.
     * @throws EnumInvalidArgumentException si el valor no es válido o es nulo.
     */
    public Authority parseAuthority(String value) throws EnumInvalidArgumentException {
        try {
            return Authority.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            log.error("Valor inválido para Authority: {}", value, e);
            throw new EnumInvalidArgumentException("authority", value, Authority.class);
        }
    }

    public String fixEncoding(String input) {
        if (input == null) return null;
        try {
            // Reinterpreta el texto como ISO-8859-1 y lo convierte a UTF-8
            return new String(input.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return input; // en caso de error, regresa el original
        }
    }





}
