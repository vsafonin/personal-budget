package ru.vladimir.personalaccounter.controller;

import java.util.Locale;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.exception.BadAppUserFormatException;
import ru.vladimir.personalaccounter.repository.ActivationUuidRepostitory;
import ru.vladimir.personalaccounter.service.UserService;

/**
 *  The Controller for work with Registration process in our app. 
 * <p>
 * This class handles the the registration process, when user send GET request
 * we return page with empty user. When user fill out the registration form, 
 * he send POST request to /registration, we use userService for create user and send email with activation link
 * </p>
 * @author Vladimir Afonin
 *
 */
@Controller
public class RegistrationController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ActivationUuidRepostitory activationUuidRepostitory;
    
    /**
     * save user in DB
     * @param user - user what we want to registration in app
     * @param bindingResult
     * @param locale
     * @param model
     * @return - login page
     */
    @PostMapping("/registration")
    public String registrationUser(@Valid @ModelAttribute("newUser") AppUser user, BindingResult bindingResult,  Locale locale, Model model) {
        if (bindingResult.hasErrors()) {
            return "login";
        }
        try {
	        userService.register(user,locale);
	        return "registration-confirmation";
        }
        catch (BadAppUserFormatException exp) {
            return "login";
        }
    }

    /**
     * method for activate user after registration. 
     * @param uuid - unique UUID for activate use, this UUID was send via email in registration 
     * @return - error-activation-page if UUID is invalid, otherwise redirect to /login page
     */
    @GetMapping("/registration/{uuid}")
    public String activateUserByUUID(@PathVariable String uuid) {
    	//find uuid in uuid repo
    	AppUser userInDb = activationUuidRepostitory.findByUuid(uuid);
    	if (userInDb != null) {
    		userInDb.setEnabled(true);
    		if (userService.save(userInDb) == null) {
    			throw new RuntimeException("хотя ссылка активации верная, почемуто не возможно сохранить..");
    		}
    	
    	}
    	else {
    		return "error-activation-page";
    	}
    	
    	return "redirect:/login";
    }
    
}
