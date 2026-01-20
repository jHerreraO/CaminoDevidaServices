package com.revoktek.services.service;

import com.revoktek.services.model.Category;
import com.revoktek.services.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CancellationException;

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


}
