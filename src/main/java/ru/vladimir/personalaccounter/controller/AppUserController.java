package ru.vladimir.personalaccounter.controller;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

	//TODO ИСПРАВИТЬ ЭТО, ЭТО передаваться должен пользователь с thymeleaf а не вот это вот все
	/**
	 * controller change user
	 */
	@PostMapping(value = "/user/{id}")
	public String userChange(@PathVariable(name = "id") String userId,
			@ModelAttribute("username") String username,
			@ModelAttribute("displayName") String displayName,
			@ModelAttribute("email") String email,
			@RequestParam(value = "changePassword", defaultValue = "off") String changePassword,
			@ModelAttribute("oldPassword") String oldPassword,
			@ModelAttribute("newPassword") String newPassword,
			@ModelAttribute("confirmPassword") String confirmPassword,
			final Model model,
			Locale locale) {
		// secondary fields
		boolean emailChanged = false;
		String oldEmail = null;
		
		// check old password with new
		// get not modified user
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String oldUserName = authentication.getName();
		AppUser oldUser = userService.findByUsername(oldUserName);
		
		if (oldUser == null) {
			throw new RuntimeException("User not found from current contex, but it inpossible");
		}
		
		model.addAttribute("user", oldUser);
		
		if (!passwordEncoder.matches(oldPassword, oldUser.getPassword())) {
			model.addAttribute("errorOldPassword", true);
			return "user-profile"; 
		}
		
		Set<ConstraintViolation<AppUser>> violation = null;
		
		// check user change name
		if (!oldUser.getUsername().equals(username)) {
			oldUser.setUsername(username);
			violation = validateField(violation,oldUser,"username");
		}
		//check user change displayName
		if (!oldUser.getDisplayName().equals(displayName)) {
			oldUser.setDisplayName(displayName);
			violation = validateField(violation,oldUser,"displayName");
		}
		
		// check my be user change email
		if (!oldUser.getEmail().equals(email)) {
			emailChanged = true;
			oldEmail = oldUser.getEmail();
			oldUser.setEmail(email);
			violation = validateField(violation,oldUser,"email");			
		}
		// check user change password
		if (changePassword.equals("on")) {
			oldUser.setPassword(newPassword);
			oldUser.setPasswordConfirm(confirmPassword);
			violation = validateField(violation,oldUser,"password");			
		}
		else {
			//set confirm password, this is kludge for fix validation password and confirm,
			//cause user not persist confirm password in db
			oldUser.setPasswordConfirm(oldUser.getPassword());
		}
		// validate user
		
		if (violation != null && !violation.isEmpty()) {
			//add to bindingresult error
			for (ConstraintViolation<AppUser> constraintViolation: violation) {
				model.addAttribute("err" + constraintViolation.getPropertyPath().toString()
						,constraintViolation.getMessage());
			}
			//i can't resolve question with bindindResult and
			//show errors in thymeleaf. If i ever find out how i solve my answer i fix it
			//FIXIT
			if (changePassword.equals("on")) {
				if (newPassword != null && !newPassword.equals(confirmPassword)) {
					model.addAttribute("errConfirmPassword", true);
				}
			}
			return "user-profile";
		}
		
		if (emailChanged) {
			//send activation link
			userService.changeEmail(email, locale);

			//add param to returned page
			model.addAttribute("emailChanged", emailChanged);
			
			//return old email, we are change it when user activate new Email
			oldUser.setEmail(oldEmail);
		}
		//encode user password
		if (changePassword.equals("on")) {
			oldUser.setPassword(passwordEncoder.encode(newPassword));
		}
		
		//save user
		userService.save(oldUser);
				
		
		// redirect back to user profile with message success changes
		model.addAttribute("success", true);
		return "user-profile";
	}
	
	/**
	 *helper method for validate field and add to set
	 * @param violation - Set of violations for current user
	 * @param the oldUser that we are validating
	 * @param fieldName 
	 * @return modified set
	 */
	private Set<ConstraintViolation<AppUser>> validateField(Set<ConstraintViolation<AppUser>> violation, AppUser oldUser, String fieldName) {
		if (violation == null) {
			violation = validator.validateProperty(oldUser, fieldName);
		}
		else {
			violation.addAll(validator.validateProperty(oldUser, fieldName));
		}
		return violation;
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
