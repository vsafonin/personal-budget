package ru.vladimir.personalaccounter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.vladimir.personalaccounter.entity.Category;
import ru.vladimir.personalaccounter.entity.Product;
import ru.vladimir.personalaccounter.repository.CategoryRepository;
import ru.vladimir.personalaccounter.repository.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;

	@Override
	public Optional<Product> findByNameAndCategory(Product product) {
		return productRepository.findByNameAndCategory(product.getName(), product.getCategory());
		
	}

	@Override
	public Product save(Product product) {
		
		Optional<Product> theProductOptional = productRepository.productWithNotSpecifiedCetegory(product.getName());
		
		if (theProductOptional.isPresent()) {
			Product theProductInDb = theProductOptional.get();
			//find category in db
			if (product.getCategory() == null) {
				throw new RuntimeException("category in product is null, it's impossible, product name: " + product.getName());
			}
			
			String categoryName = product.getCategory().getName();
			Optional<Category> theCategoryOptional = categoryRepository.findByName(categoryName);
			if (theCategoryOptional.isPresent()) {
				theProductInDb.setCategory(theCategoryOptional.get());
			}
			else {
				theProductInDb.setCategory(categoryRepository.save(new Category(categoryName)));
			}			
			
			return productRepository.save(theProductInDb);
		}
		return productRepository.save(product);
	}

	@Override
	public Optional<Product> findByName(Product product) {
		List<Product> products = productRepository.findAllByName(product.getName());			
				
		if (products.size() == 1) {
			return Optional.of(products.get(0));
		}
		return Optional.empty();
	}
	
	
}
