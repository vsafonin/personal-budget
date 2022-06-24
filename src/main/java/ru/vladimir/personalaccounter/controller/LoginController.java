package ru.vladimir.personalaccounter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ru.vladimir.personalaccounter.entity.AppUser;
/**
 * This controller add an empty user to the
 * model when requesting /login
 * @author Vladimir Afonin
 *
 */
@Controller
public class LoginController {
    
    @GetMapping("/login")
    public String getLoginAndRegistrationForm(Model model){
        AppUser newUser = new AppUser();
        model.addAttribute("newUser", newUser);
        return "login";
    }
    
    
}
