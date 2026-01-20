package com.revoktek.services.controller;

import com.revoktek.services.service.CategoryService;
import com.revoktek.services.utils.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/category")
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

    /**
     * Elimina categoria por su ID.
     *
     * @param idCategory ID del día festivo a eliminar.
     * @return ResponseEntity con un mensaje de éxito.
     */
    @DeleteMapping("/{idCategory}")
    public ResponseEntity<Message> deleteById(@PathVariable Long idCategory) {
        categoryService.deleteById(idCategory);
        return ResponseEntity.ok(
                new Message(true, "Día festivo eliminado con éxito")
        );
    }

}
