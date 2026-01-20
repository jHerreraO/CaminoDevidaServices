package com.revoktek.services.service;

import com.revoktek.services.model.Category;
import com.revoktek.services.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Log4j2
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<Category> findAll(){
        return categoryRepository.findAll();
    }

    private Boolean existByName(String name){
        return categoryRepository.existsByNameCategory(name);
    }

    public void save(String namecategory){
        if (!existByName(namecategory)){
            Category category = Category.builder().nameCategory(namecategory).build();
            categoryRepository.save(category);
        }
    }

    public void deleteById(Long idCategory){
        categoryRepository.deleteById(idCategory);
    }

    /**
     * Inicializa las categorías base del sistema.
     *
     * Reglas:
     * - No duplica categorías
     * - Se valida por nombre
     * - Método idempotente (puede ejecutarse múltiples veces)
     */
    public void initDefaultCategories() {

        log.info("🔵 Initializing default categories");

        List<String> defaultCategories = List.of(
                "Teología",
                "Alpha",
                "Mujeres",
                "Hombres",
                "Escuela para padres",
                "Matrimonios",
                "Crecimiento Espiritual",
                "GP Fit"
        );

        defaultCategories.forEach(this::save);

        log.info("🟢 Default categories checked or created successfully");
    }


}
