package ru.vladimir.personalaccounter.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.vladimir.personalaccounter.entity.Category;

/**
 *
 * @author vladimir
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

	Optional<Category> findByName(String name);
    
}
