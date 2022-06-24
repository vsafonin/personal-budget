package ru.vladimir.personalaccounter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.vladimir.personalaccounter.entity.Category;
import ru.vladimir.personalaccounter.entity.Product;

/**
 *
 * @author vladimir
 */
public interface ProductRepository extends JpaRepository<Product,Long> {
	
    Optional<Product> findByNameAndCategory(@Param("name") String name,@Param("category") Category category);

    @Query("select p from Product p where p.name=:name and p.category.name = 'not specified'")
    Optional<Product> productWithNotSpecifiedCetegory(@Param("name") String name);

    @Query("select p from Product p where p.name= ?1")
	List<Product> findAllByName(String name);
}
