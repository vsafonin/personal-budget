package ru.vladimir.personalaccounter.service;

import java.util.Optional;

import ru.vladimir.personalaccounter.entity.Product;

public interface ProductService {

	Optional<Product> findByNameAndCategory(Product product);

	Product save(Product product);

	Optional<Product> findByName(Product product);

}
