package ru.vladimir.personalaccounter.controllerAdvice;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.UserService;

@ControllerAdvice
public class RequiredAttrebuteControllerAvice {

	@Autowired
	private BankAccountService accountService;
	@Autowired
	private UserService userService;
	private static final String[] ignorePath = {"/login","/registration","/error"};

	@ModelAttribute
	public void addUserAndBankToAllModel(HttpServletRequest request, Model model) {
		boolean ignore = false;
		String path = request.getServletPath();
		ignore = Arrays.stream(ignorePath).anyMatch( p -> path.startsWith(p)); // /login/logout - must be true
		
		if (!ignore) { //i think not necessary add user to model to login or logout or register page 
			AppUser currentUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
			model.addAttribute("user", currentUser);
			model.addAttribute("bankAccounts", accountService.getBankAccounts());
		}
	}
}
