package com.revoktek.services.controller;

import com.revoktek.services.service.CategoryService;
import com.revoktek.services.utils.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Service
@Log4j2
public class CategoryController {
    private final CategoryService categoryService;

    /**
     * Obtiene la lista de todas las categrias registradas en el sistema.
     *
     * @return ResponseEntity con un mensaje de éxito y la lista de empresas en el campo 'data'.
     */
    @GetMapping
    public ResponseEntity<Message> findAll() {
        return ResponseEntity.ok(new Message(true, "Todas las empresas registradas", categoryService.findAll()));
    }

}
