package ru.vladimir.personalaccounter.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ru.vladimir.personalaccounter.entity.Product;
import ru.vladimir.personalaccounter.repository.ProductRepository;

@RestController
@RequestMapping(value = "/api")
public class ProductRestController {
	@Autowired
	private ProductRepository productRepository;
	
	@GetMapping("/product")
	public List<String> getProductFromDb(@RequestParam(value = "q", required = false,defaultValue = "")
	String q ) {
		List<String> result = new ArrayList<>();
        
        List<Product> shopFromDb;
        
        if (q.isBlank()) {
            shopFromDb = productRepository.findAll().stream().limit(10).collect(Collectors.toList());
        }
        else {
            shopFromDb = productRepository.findAll().stream()
                    .filter(c -> c.getName().contains(q))
                    .limit(10).collect(Collectors.toList());
        }
        
        shopFromDb.stream().forEach(c -> result.add(c.getName()));
        return result;
	}
}
