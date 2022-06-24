/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package ru.vladimir.personalaccounter.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ru.vladimir.personalaccounter.entity.Category;
import ru.vladimir.personalaccounter.entity.Product;

/**
 *
 * @author vladimir
 */
@SpringBootTest
public class ProductRepositoryIT {

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public ProductRepositoryIT() {
    }

    @Test
    public void testProductShoildBeHasCategoryInDb() {
        //we need to create category
        Category category = new Category();
        category.setName("продукты");
        categoryRepository.save(category);
        Category category1 = new Category();
        category1.setName("хозтовары");
        categoryRepository.save(category1);
        //we need to create product
        Product product = new Product();
        product.setName("test123");
        product.setCategory(category);
        productRepository.save(product);

        Product product2 = new Product();
        product2.setName("test2123");
        product2.setCategory(category1);
        productRepository.save(product2);
        //get product from db
        List<Product> productFromDbSet = productRepository.findAll();
        assertThat(productFromDbSet.isEmpty()).isFalse();
        for (Product productItem : productFromDbSet) {
            if (productItem.getName().equals("test123")) {
                assertThat(productItem.getCategory().getName()).isEqualTo(category.getName());
            }
            if (productItem.getName().equals("test2123")) {
                assertThat(productItem.getCategory().getName()).isEqualTo(category1.getName());
            }
        }
    }

}
