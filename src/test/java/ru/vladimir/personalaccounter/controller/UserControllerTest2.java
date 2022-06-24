package ru.vladimir.personalaccounter.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import ru.vladimir.personalaccounter.MockConfiguration;
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.Authorities;
import ru.vladimir.personalaccounter.enums.AauthoritiesEnum;
import ru.vladimir.personalaccounter.repository.ActivationEmailRepository;
import ru.vladimir.personalaccounter.repository.RoleRepository;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.UserService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AppUserController.class)
@Import(MockConfiguration.class)
class UserControllerTest2 {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private UserService userService;
	
	@MockBean
	private RoleRepository roleRepository;
	
	@MockBean
	private PasswordEncoder passwordEncoder;
	
	@MockBean
	private ActivationEmailRepository activationEmailRepository;
	
	@MockBean
	private BankAccountService accountService;
	
	

	@Mock
	private Authentication auth;
	
	
	@Test
	@WithMockUser(roles = "user",username = "pupa",password = "$2a$12$pe3LzLhADOZ.Gg627JJVLOSPA/q.c5sC9059qDtiwORGo49qVCa3S")
	void testChangeUserNameShouldBeFail() throws Exception {
		//create user string
		String userName = "p";
		String oldPassword = "password";
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		when(userService.findByUsername("pupa")).thenReturn(theTestUser);
		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
		
		mockMvc.perform(post("/user/1")
				.content(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.param("username", userName)
				.param("oldPassword",oldPassword)
				.param("displayName", theTestUser.getDisplayName())
				.param("email", theTestUser.getEmail())				
				.flashAttr("user", theTestUser)
				.with(csrf())
				)
		.andExpect(view().name("user-profile"))
		.andExpect(model().attributeExists("errusername"));
		
	}
	
	@Test
	@WithMockUser(roles = "user",username = "pupa",password = " $2a$12$pe3LzLhADOZ.Gg627JJVLOSPA/q.c5sC9059qDtiwORGo49qVCa3S ")
	void testChangeUserNameShouldBeOk() throws Exception {
		//create user string
		String userName = "pupa";
		String oldPassword = "password";
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		when(userService.findByUsername("pupa")).thenReturn(theTestUser);
		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
		
		mockMvc.perform(post("/user/1")
				.content(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.param("username", userName)
				.param("oldPassword",oldPassword)
				.param("displayName", theTestUser.getDisplayName())
				.param("email", theTestUser.getEmail())				
				.flashAttr("user", theTestUser)
				.with(csrf())
				)
		.andExpect(view().name("user-profile"))
		.andExpect(model().attribute("success",true));
		verify(userService,times(1)).save(any());
		
	}
	
	@Test
	@WithMockUser(roles = "user",username = "pupa",password = " $2a$12$pe3LzLhADOZ.Gg627JJVLOSPA/q.c5sC9059qDtiwORGo49qVCa3S ")
	void testChangeDisplayNameShouldBeFail() throws Exception {
		//create user string
		String displayName = "p";
		String oldPassword = "password";
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		when(userService.findByUsername("pupa")).thenReturn(theTestUser);
		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
		
		mockMvc.perform(post("/user/1")
				.content(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.param("username", theTestUser.getUsername())
				.param("oldPassword",oldPassword)
				.param("displayName", displayName)
				.param("email", theTestUser.getEmail())				
				.flashAttr("user", theTestUser)
				.with(csrf())
				)
		.andExpect(view().name("user-profile"))
		.andExpect(model().attributeExists("errdisplayName"));
		
	}
	
	@Test
	@WithMockUser(roles = "user",username = "pupa",password = " $2a$12$pe3LzLhADOZ.Gg627JJVLOSPA/q.c5sC9059qDtiwORGo49qVCa3S ")
	void testChangeDisplayNameShouldBeOk() throws Exception {
		//create user string
		String displayName = "pupa";
		String oldPassword = "password";
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		
		Authorities authorities = new Authorities();
		authorities.setName(AauthoritiesEnum.USER.getRoleName());
		theTestUser.setRoles(Collections.singleton(authorities));
		
		when(userService.findByUsername("pupa")).thenReturn(theTestUser);
		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
		when(auth.getPrincipal()).thenReturn(theTestUser);
		
		SecurityContextHolder.getContext().setAuthentication(auth);
		mockMvc.perform(post("/user/1")
				.content(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.param("username", theTestUser.getUsername())
				.param("oldPassword",oldPassword)
				.param("displayName", displayName)
				.param("email", theTestUser.getEmail())				
				.flashAttr("user", theTestUser)
				.with(csrf())
				.with(user(theTestUser))
				)
		.andExpect(view().name("user-profile"))
		.andExpect(model().attribute("success",true));
		verify(userService,times(1)).save(any());
		
	}
	
	@Test
	@WithMockUser(roles = "user",username = "pupa",password = " $2a$12$pe3LzLhADOZ.Gg627JJVLOSPA/q.c5sC9059qDtiwORGo49qVCa3S ")
	void testOldPasswordShouldBeFail() throws Exception {
		//create user string
		String oldPassword = "password";
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		when(userService.findByUsername("pupa")).thenReturn(theTestUser);
		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
		
		mockMvc.perform(post("/user/1")
				.content(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.param("oldPassword",oldPassword)
				.flashAttr("user", theTestUser)
				.with(csrf())
				)
		.andExpect(view().name("user-profile"))
		.andExpect(model().attributeExists("errorOldPassword"));
		
	}
	
	//old password doesn't need testing, because this testing in all test with ok statuse
	
	@Test
	@WithMockUser(roles = "user",username = "pupa",password = "$2a$12$pe3LzLhADOZ.Gg627JJVLOSPA/q.c5sC9059qDtiwORGo49qVCa3S ")
	void testChangeEmailShouldBeFail() throws Exception {
		//create user string
		String email = "badMailAddr";
		String oldPassword = "password";
		Authorities authorities = new Authorities();
		authorities.setName(AauthoritiesEnum.USER.getRoleName());
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		theTestUser.setRoles(Collections.singleton(authorities));
		when(userService.findByUsername("pupa")).thenReturn(theTestUser);
		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
		when(auth.getPrincipal()).thenReturn(theTestUser);
		SecurityContextHolder.getContext().setAuthentication(auth);
		mockMvc.perform(post("/user/1")
				.content(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.param("email", email)
				.param("oldPassword",oldPassword)
				.flashAttr("user", theTestUser)
				.with(csrf())
				.with(user(theTestUser))
				)
		.andExpect(view().name("user-profile"))
		.andExpect(model().attributeExists("erremail"));
		
	}

	@Test
	@WithMockUser(roles = "user",username = "pupa",password = "$2a$12$pe3LzLhADOZ.Gg627JJVLOSPA/q.c5sC9059qDtiwORGo49qVCa3S")
	void testChangeEmailShouldBeOk() throws Exception {
		//create user string
		String email = "goodmail@mail.com";
		String oldPassword = "password";
		AppUser theTestUser = new AppUser("pupa","pupa","password","password","pupa@mail.ru",true);
		when(userService.findByUsername("pupa")).thenReturn(theTestUser);
		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
		
		mockMvc.perform(post("/user/1")
				.content(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.param("username", theTestUser.getUsername())
				.param("oldPassword",oldPassword)
				.param("displayName", theTestUser.getDisplayName())
				.param("email", email)
				.param("oldPassword",oldPassword)
				.flashAttr("user", theTestUser)
				.with(csrf())
				)
				.andExpect(view().name("user-profile"))
				.andExpect(model().attribute("success",true))
				.andExpect(model().attribute("emailChanged",true));
		
		verify(userService,times(1)).changeEmail(any(),any());
		
	}
	
	/**
	 * test activate user
	 */
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testChangeEmailAndActivateItShouldBeOk() throws Exception{
		String uuid = "testUID";
		String email = "success@mail.ru";
		AppUser theTestUser = new AppUser("pupa", "pupa","password", "password", "pupa@mail.ru", true);
		when(activationEmailRepository.findAppUserByUUID(uuid)).thenReturn(theTestUser);
		when(activationEmailRepository.findEmailByUUID(uuid)).thenReturn(email);
		
		mockMvc.perform(get("/email-changing/" + uuid)			
				)
		.andExpect(view().name("email-changed-confirmation"));
		assertThat(theTestUser.getEmail().equals(email));
	}
	
	
	@Test
	void testUserCanChangePassword() throws Exception{
		mockMvc.perform(get("/recover")).andExpect(view().name("recover"));
	}
	
	@Test
	void testUserCanChangePasswordInvalidEmail() throws Exception {
		mockMvc.perform(post("/recover").param("email", "").with(csrf()))
			.andExpect(view().name("recover"))
			.andExpect(model().attributeExists("error"));
	}
	@Test
	void testUserCanChangePasswordInvalidEmailNotEmpty() throws Exception {
		String testMail = "test@mail.ru";
		when(userService.findByEmail(testMail)).thenReturn(null);
		mockMvc.perform(post("/recover").param("email", testMail).with(csrf()))
		.andExpect(view().name("recover-success"));
		verify(userService,times(0)).recoverUserPassword(any(AppUser.class),any());
	}
	@Test
	void testUserCanChangePasswordEmailOk() throws Exception {
		String testMail = "test@mail.ru";
		when(userService.findByEmail(testMail)).thenReturn(new AppUser());
		mockMvc.perform(post("/recover").param("email", testMail).with(csrf()))
		.andExpect(view().name("recover-success"));
		verify(userService,times(1)).recoverUserPassword(any(AppUser.class), any());
	}
	@Test
	void testUserCanChangePasswordPhase2InvalidToken() throws Exception {
		String invalidToken = "token";
		when(userService.findByAppUserRecoveryModel(invalidToken)).thenReturn(null);
		mockMvc.perform(get("/recover/" + invalidToken))
		.andExpect(view().name("recover"))
		.andExpect(model().attributeExists("error"));
		
	}
	@Test
	void testUserCanChangePasswordPhase2ShouldBeOk() throws Exception {
		String invalidToken = "token";
		when(userService.findByAppUserRecoveryModel(invalidToken)).thenReturn(new AppUser());
		mockMvc.perform(get("/recover/" + invalidToken))
		.andExpect(view().name("recover-change-password-page"));
		
	}
	@Test
	void testUserCanChangePasswordPhase2PostInvalidPasswordAndConfirm() throws Exception {
		AppUser theAppUser = new AppUser();
		theAppUser.setUsername("test");
		theAppUser.setEmail("test@mail.ru");
		theAppUser.setDisplayName("test");
		theAppUser.setPassword("slakd;lasd;ada");
		theAppUser.setPasswordConfirm("another");
		mockMvc.perform(post("/recover/change-password").with(csrf()).flashAttr("theUser", theAppUser))
		.andExpect(view().name("recover-change-password-page"));
		
		
	}
	@Test
	void testUserCanChangePasswordPhase2PostShouldBeOk() throws Exception {
		AppUser theAppUser = new AppUser();
		theAppUser.setUsername("test");
		theAppUser.setEmail("test@mail.ru");
		theAppUser.setDisplayName("test");
		String password = "sa;ld;lK292";
		theAppUser.setPassword(password);
		theAppUser.setPasswordConfirm(password);
		mockMvc.perform(post("/recover/change-password").with(csrf()).flashAttr("theUser", theAppUser))
		.andExpect(redirectedUrl("/login"))
		.andExpect(model().hasNoErrors());
		
		verify(userService, times(1)).changePassword(theAppUser);
		
	}

}
