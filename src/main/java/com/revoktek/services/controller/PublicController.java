package com.revoktek.services.controller;


import com.revoktek.services.service.UserService;
import com.revoktek.services.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * Controlador REST público para endpoints accesibles sin autenticación previa.
 * <p>
 * Actualmente provee la funcionalidad para refrescar tokens JWT.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public")
public class PublicController {

    private static final Logger log = LoggerFactory.getLogger(PublicController.class);
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * Endpoint para refrescar el token JWT enviado en el header Authorization.
     * <p>
     * El header debe estar presente y comenzar con "Bearer ".
     * Si es válido, devuelve un nuevo token en el header Authorization.
     * En caso de error, devuelve Bad Request con el mensaje en el header "error".
     * </p>
     *
     * @param request HttpServletRequest que contiene el header Authorization con el token actual.
     * @return ResponseEntity sin cuerpo, con el token refrescado en el header Authorization o
     *         con error en headers en caso de fallo.
     */
    @GetMapping("/refreshToken")
    public ResponseEntity<Void> refreshToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        String BEARER = "Bearer ";
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER)) {
            return ResponseEntity.badRequest()
                    .header("exception", "HeaderNotPresentException")
                    .header("error", "Authorization header not present or not start with Bearer")
                    .build();
        }
        try {
            String token = jwtUtil.getRefreshedToken(authorizationHeader.substring(BEARER.length()));
            return ResponseEntity.ok().header(AUTHORIZATION, BEARER + token).build();
        } catch (Exception e) {
            log.error("Error refreshing JWT token: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .header("error", e.getMessage())
                    .build();
        }
    }
}
