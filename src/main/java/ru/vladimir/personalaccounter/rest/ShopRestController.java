package ru.vladimir.personalaccounter.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ru.vladimir.personalaccounter.entity.Shop;
import ru.vladimir.personalaccounter.repository.ShopRepository;

@RestController
@RequestMapping(value = "/api")
public class ShopRestController {

	@Autowired
	private ShopRepository shopRepository;
	
	@GetMapping("/shop")
	public List<String> getShopFromDb(@RequestParam(value = "q", required = false,defaultValue = "")
	String q ) {
		List<String> result = new ArrayList<>();
        
        List<Shop> shopFromDb;
        
        if (q.isBlank()) {
            shopFromDb = shopRepository.findAll().stream().limit(10).collect(Collectors.toList());
        }
        else {
            shopFromDb = shopRepository.findAll().stream()
                    .filter(c -> c.getName().contains(q))
                    .limit(10).collect(Collectors.toList());
        }
        
        shopFromDb.stream().forEach(c -> result.add(c.getName()));
        return result;
	}
	
}
