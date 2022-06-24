package ru.vladimir.personalaccounter.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.service.UserService;

/**
 *
 * @author vladimir
 */
@RestController
@RequestMapping(value = "/api")
public class AppUserRestController {
    
	@Autowired
	private UserService userService;
	
    @GetMapping("/getUserName")
    public String getUserName(){
        AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
        return theAppUser.getUsername();
    }
}
