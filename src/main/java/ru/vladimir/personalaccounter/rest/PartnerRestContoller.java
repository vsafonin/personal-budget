package ru.vladimir.personalaccounter.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.Partner;
import ru.vladimir.personalaccounter.repository.PartnerRepository;
import ru.vladimir.personalaccounter.service.UserService;

@RestController
@RequestMapping(value = "/api")
public class PartnerRestContoller {

		@Autowired
		private PartnerRepository partnerRepository;
		
		@Autowired
		private UserService userService;
		
		@GetMapping("/partner")
		public List<String> getPartnerFromDb(@RequestParam(value = "q", required = false,defaultValue = "") String q) 
			{
				List<String> result = new ArrayList<>();
		        
		        List<Partner> pathnerInDb;
		        AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		        
		        if (q.isBlank()) {
		            pathnerInDb = partnerRepository.findAll().stream().limit(10).collect(Collectors.toList());
		        }
		        else {
		            pathnerInDb = partnerRepository.findAll().stream()
		            		.filter(c -> c.getAppUser().equals(theAppUser))
		                    .filter(c -> c.getName().contains(q))
		                    .limit(10).collect(Collectors.toList());
		        }
		        
		        pathnerInDb.stream().forEach(c -> result.add(c.getName()));
		        return result;
		}
		
	
}
