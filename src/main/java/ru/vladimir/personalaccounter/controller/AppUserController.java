package ru.vladimir.personalaccounter.controller;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.exception.UserNotFoundException;
import ru.vladimir.personalaccounter.repository.ActivationEmailRepository;
import ru.vladimir.personalaccounter.service.UserService;
/**
 * The Controller for work with AppUser Entity over web UI.
 * <p>
 * This class handles the CRUD operations for AppUser entity via HTTP actions
 * as result returns HTML based web pages using thymeleaf template.
 * </p>
 * @author Vladimir Afonin
 *
 */
@Controller
public class AppUserController {

	@Autowired
	private UserService userService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private Validator validator;
	
    @Autowired
    private ActivationEmailRepository activationEmail;
    
	
	/**
	 * Handles request to "/user/{id}" and return HTML page with new or existing AppUser
	 * @return - HTML page for edit AppUser object via WEB UI
	 */
	@GetMapping("/user/{id}")
	public String userProfile(@PathVariable(name = "id") Long userId, Model model) {
		// find user in db
		Optional<AppUser> userInDb = userService.findById(userId);
		if (!userInDb.isPresent())
			throw new UserNotFoundException("User id not found in db");

		// add user model
		model.addAttribute("user", userInDb.get());

		return "user-profile";
	}

	/**
	 * This controller save changed user to DB. First checks user password, next validates user fields, and if everything is OK
	 * call userService for save it.
	 * If user change email, first we are send to new email address activation link, and set old email address to appUser.email back.
	 * If user changing password, first we are encode password via PasswordEncode, and set this value to user password field.
	 * @param changePassword - if user wants to change password, he must to set changePassword radioButton, default dalue is off.
	 */
	@PostMapping(value = "/user/{id}")
	public String userChange(@PathVariable(name = "id") long userId,
			@Valid @ModelAttribute("user") AppUser theAppUser,
			BindingResult bindingResult,
			@RequestParam(value = "changePassword", defaultValue = "off") String changePassword,
			final Model model,
			Locale locale) {
		// get not modified user
		AppUser oldAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		
		if (oldAppUser == null) {
			throw new RuntimeException("User not found from current contex, but it inpossible");
		}
		
		if (!passwordEncoder.matches(theAppUser.getPasswordInputField(), oldAppUser.getPassword())) {
			bindingResult.rejectValue("passwordInputField", "error.oldpassword", "password is wrong");
			return "user-profile"; 
		}
		
		
		//check username, displayname and email address
		if (bindingResult.hasFieldErrors("username") || bindingResult.hasFieldErrors("displayName") || 
				bindingResult.hasFieldErrors("email")) {
			return "user-profile";
		}
		if (changePassword.equals("on")) {
			if (bindingResult.hasFieldErrors("password")) {
				return "user-profile";
			}
			theAppUser.setPassword(passwordEncoder.encode(theAppUser.getPassword()));
		}
		/*
		 * if user changed email, we are send to new email activation link for activation,
		 * before it, set old email.
		 */
		if (!oldAppUser.getEmail().equals(theAppUser.getEmail())) {
			userService.changeEmail(theAppUser.getEmail(), locale);
			model.addAttribute("emailChanged", true);
			theAppUser.setEmail(oldAppUser.getEmail());
		}
		userService.save(theAppUser);				
		
		// redirect back to user profile with message success changes
		model.addAttribute("success", true);
		return "user-profile";
	}
	
	/**
	 * Handles request to "/email-changing/{uuid}" and if OK save new email address to user field.
	 * for activate new email address we use UUID, this UUID we sent by
	 * email when the user requested a change of email. 
	 * @param uuid - UUID from email message for activate the new email.
	 * @return - if OK return page with success information. 
	 */
	@GetMapping(value = "/email-changing/{uuid}")
	public String changeEmail(@PathVariable("uuid") String uuid) {
		AppUser user = activationEmail.findAppUserByUUID(uuid);
		if (user == null) {
			return "error-activation-page";
		}
		else {
			user.setEmail(activationEmail.findEmailByUUID(uuid));
			userService.save(user);
		}
		return "email-changed-confirmation";
	}
	
	/**
	 * Handles request to "/user/generatejwt", calls to service layer to generate new JWT token for telegram bot
	 * @param model
	 * @return - redirect to user edit page again.
	 */
	@GetMapping(value = "/user/generatejwt")
	public String generateJwtToken(Model model) {
		AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		userService.generateJwtToken(theAppUser);
		return "redirect:/user/" + theAppUser.getId();
	}
	
	/**
	 * Handles request to "/user/deleteJwt/{id}", calls to service layer to delete JWT token.
	 * @param jwtTokenId - JWT token ID
	 * @return -  redirect to user edit page again.
	 */
	@GetMapping(value = "/user/deleteJwt/{id}")
	public String deleteJwtToken(@PathVariable("id") long jwtTokenId) {
		AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		userService.deleteJwt(jwtTokenId, theAppUser);
		return "redirect:/user/" + theAppUser.getId();
	}
	

	/**
	 * Handles requests to "/logout" and call SecurityContextLogoutHandler,
	 * method for logout user use GET request (default /logout in spring work only with post method)
	 */
	@GetMapping("/logout")
	public String logoutUser(HttpServletRequest request, HttpServletResponse response) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			new SecurityContextLogoutHandler().logout(request, response, authentication);
		}
		return "redirect:/login?logout";
	}

	/**
	 * Handles request to "/recover" page, return special "recover" page, where users can restore their passwords
	 * @return - HTML page for recover user passwords
	 */
	@GetMapping("/recover")
	public String getRoceverPage() {
		return "recover";
	}
	
	/**
	 * Handles POST request to "/recover, if everything is OK generate UUID and send to user's email. 
	 * @param model
	 * @param email - User's email, an email with instructions will be sent there.
	 * @param locale - User's locale in browser.
	 * @return - if everything is OK return success information.
	 */
	@PostMapping("/recover")
	public String recoverPassword(Model model, @RequestParam String email, Locale locale) {
		if (email.isBlank()) {
			model.addAttribute("error","Email is empty");
			return "recover"; 
		}
		AppUser theAppUser = userService.findByEmail(email);
		if (theAppUser != null ) {
			//generate token
			userService.recoverUserPassword(theAppUser, locale);
		}
	
		return "recover-success";
	}
	/**
	 * Handles request to "/recover/{id}", where id is token that generated in userService.recoverUserPassword(theAppUser, locale);
	 * if everything is OK sends AppUser from DB, and reset his password.
	 * @param token - UUID token which generated in userService.recoverUserPassword(theAppUser, locale);
	 * @param model
	 * @return - special HTML page for create new password. 
	 */
	@GetMapping("/recover/{id}")
	public String recoverPasswordPhase2(@PathVariable("id") String token, Model model) {
		AppUser theAppUser = userService.findByAppUserRecoveryModel(token);
		if (theAppUser == null) {
			model.addAttribute("error", "Token is expired"); //TODO добавить на русском
			return "recover";
		}
		theAppUser.setPassword(null); //если не поставить пользователю выкидывается хеш от пароля, это не очень круто
		model.addAttribute("theUser", theAppUser );
		return "recover-change-password-page";
	}
	/**
	 * Handles POST request to "/recover/change-password", validate new user password and password confirm, if everythig
	 * is ok, call userService for save changes. Return redirect to /login or recover-change-password-page again if has error
	 * @param model
	 * @param appUser - AppUser which changing password.
	 * @return Return redirect to /login or recover-change-password-page again if has error
	 */
	@PostMapping("/recover/change-password")
	public String recoverPasswordChange(Model model, @ModelAttribute("theUser") AppUser appUser) {
		if (!appUser.getPassword().equals(appUser.getPasswordConfirm())) {
			model.addAttribute("error","passwords don't match");
			return "recover-change-password-page";
		}
		Set<ConstraintViolation<AppUser>> violations = validator.validateProperty(appUser, "password");
		if (violations != null &&  !violations.isEmpty()) {
			model.addAttribute("error", 
					"password is weak, password must be greater 8 char and has 1 digt and 1 upper case letter");
			return "recover-change-password-page";
		}
		userService.changePassword(appUser);
		return "redirect:/login";
	}
	
}
