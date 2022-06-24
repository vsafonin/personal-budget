package ru.vladimir.personalaccounter.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.exception.UserGetDataSecurityExp;
import ru.vladimir.personalaccounter.methods.JwtProvider;
import ru.vladimir.personalaccounter.repository.ActivationEmailRepository;
import ru.vladimir.personalaccounter.repository.AppUserJwtRepository;
import ru.vladimir.personalaccounter.repository.RoleRepository;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.UserService;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = AppUserController.class)
class AppUserControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private UserService userService;
	
	@MockBean
	private UserDetailsService userDetailsService;
	
	@MockBean
	private RoleRepository roleRepository;
	
	@MockBean
	private ActivationEmailRepository activationEmailRepository;
	
	@MockBean
	private BankAccountService bankAccountService;
	
	@MockBean
	private AppUserJwtRepository appUserJwtRepository;
	
	@MockBean
	private JwtProvider jwtProvider;
	
	@Mock
    private Authentication auth;
	
	
	/*
	 * Тестирую что пользователь может открыть личный кабинет
	 */
	@Test
	@WithMockUser(roles = "user",username = "pupa")
	void testUserPersonalArea() throws Exception {
		Optional<AppUser> theAppUserStoredInDb = Optional.of(new AppUser());
		when(userService.findById(1L)).thenReturn(theAppUserStoredInDb);
		
		mockMvc.perform(get("/user/1"))
			.andExpect(view().name("user-profile"))
			.andExpect(model().attributeExists("user"));
	}
	
	/*
	 * Тестирую что кто угодно не может получить доступ в личный кабинет пользовател, только владелец
	 * так себе тест, так как исключение я генерирую сам.
	 */
	@Test	
	void testUserPersonalArea_will_be_fail() throws Exception {
		when(userService.findById(anyLong())).thenThrow(UserGetDataSecurityExp.class);
		
		//пробуем получить личный кабинет пользователя
		mockMvc.perform(get("/user/1")).andExpect(redirectedUrl("http://localhost/login"));
		
	}
	
	/*
	 * Тестирую что пользователь может сохранить информацию только введя верный пароль.
	 */
	@Test
	@WithMockUser(username = "pupa", roles = "user")
	void testUserSave_should_be_fail_wrong_password() throws Exception {
		AppUser theAppUserStoredInDb = new AppUser();
		theAppUserStoredInDb.setPassword("$2a$12$cgnuhz85WrFxgqe2H/n4Su70oG5aaJn4POrn.NOceFiARytFC5oQu");
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theAppUserStoredInDb);
		theAppUserStoredInDb.setPasswordInputField("SALdksadmaskk322");
		
		mockMvc.perform(post("/user/1").with(csrf()).flashAttr("user", theAppUserStoredInDb))
				.andExpect(view().name("user-profile"))
				.andExpect(model().attributeHasFieldErrors("user","passwordInputField"));
	}
	
	/*
	 * Тестирую валидацию имени пользователи (ввод недопустимого)
	 */
	@Test
	@WithMockUser(username = "pupa", roles = "user")
	void testUserSave_should_be_fail_wrongUserName() throws Exception {
		AppUser theAppUserStoredInDb = new AppUser();
		theAppUserStoredInDb.setPassword("$2a$12$cgnuhz85WrFxgqe2H/n4Su70oG5aaJn4POrn.NOceFiARytFC5oQu"); // encrypted value of Asldkdsamw212
		
		theAppUserStoredInDb.setPasswordInputField("Asldkdsamw212"); 
		theAppUserStoredInDb.setUsername("2"); //this value isn't accepted
		
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theAppUserStoredInDb);
		mockMvc.perform(post("/user/1").with(csrf()).flashAttr("user", theAppUserStoredInDb))
		.andExpect(view().name("user-profile"))
		.andExpect(model().attributeHasFieldErrors("user","username"));
	}
	
	/*
	 * тестирую валидацию ввода email (ввод недопустимого)
	 */
	@Test
	@WithMockUser(username = "pupa", roles = "user")
	void testUserSave_should_be_fail_wrong_wnail() throws Exception {
		AppUser theAppUserStoredInDb = new AppUser();
		theAppUserStoredInDb.setPassword("$2a$12$cgnuhz85WrFxgqe2H/n4Su70oG5aaJn4POrn.NOceFiARytFC5oQu"); // encrypted value of Asldkdsamw212
		
		theAppUserStoredInDb.setPasswordInputField("Asldkdsamw212"); 
		theAppUserStoredInDb.setEmail("2"); //this value isn't accepted
		
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theAppUserStoredInDb);
		mockMvc.perform(post("/user/1").with(csrf()).flashAttr("user", theAppUserStoredInDb))
		.andExpect(view().name("user-profile"))
		.andExpect(model().attributeHasFieldErrors("user","email"));
	}
	
	/*
	 * тестирую валидацию ввода нового пароля, слишком слабый
	 */
	@Test
	@WithMockUser(username = "pupa", roles = "user")
	void testUserSave_should_be_fail_weakNewPasssword() throws Exception {
		AppUser theAppUserStoredInDb = new AppUser();
		theAppUserStoredInDb.setPassword("$2a$12$cgnuhz85WrFxgqe2H/n4Su70oG5aaJn4POrn.NOceFiARytFC5oQu"); // encrypted value of Asldkdsamw212
		
		theAppUserStoredInDb.setPasswordInputField("Asldkdsamw212"); 
		theAppUserStoredInDb.setEmail("good@mail.ru");
		theAppUserStoredInDb.setUsername("good");
		theAppUserStoredInDb.setDisplayName("good");
		theAppUserStoredInDb.setPassword("bad");
		theAppUserStoredInDb.setPasswordConfirm("bad");
		
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theAppUserStoredInDb);
		mockMvc.perform(post("/user/1").with(csrf()).flashAttr("user", theAppUserStoredInDb).param("changePassword", "true"))
		.andExpect(view().name("user-profile"))
		.andExpect(model().attributeHasFieldErrors("user","password"));
	}
	/*
	 * тестирую валидацию ввода нового пароля, не совпадает с подтверждением
	 */
	@Test
	@WithMockUser(username = "pupa", roles = "user")
	void testUserSave_should_be_fail_not_equal_confirm() throws Exception {
		AppUser theAppUserStoredInDb = new AppUser();
		theAppUserStoredInDb.setPassword("$2a$12$cgnuhz85WrFxgqe2H/n4Su70oG5aaJn4POrn.NOceFiARytFC5oQu"); // encrypted value of Asldkdsamw212
		
		AppUser theNewAppUser = new AppUser();
		theNewAppUser.setPasswordInputField("Asldkdsamw212"); 
		theNewAppUser.setEmail("good@mail.ru");
		theNewAppUser.setUsername("good");
		theNewAppUser.setDisplayName("good");
		theNewAppUser.setPassword("GoodPassword212");
		theNewAppUser.setPasswordConfirm("bad");
		
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theNewAppUser, theAppUserStoredInDb);
		mockMvc.perform(post("/user/1").with(csrf()).flashAttr("user", theAppUserStoredInDb).param("changePassword", "on"))
			.andExpect(view().name("user-profile"))
			.andExpect(model().attributeHasFieldErrors("user","password"));
	}
	
	/*
	 * тестирую что если все поля заполнены верно, смена имени но нет смены пароля
	 */
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testUserSave_should_be_ok() throws Exception {
		AppUser theAppUserStoredInDb = new AppUser();
		theAppUserStoredInDb.setPassword("$2a$12$cgnuhz85WrFxgqe2H/n4Su70oG5aaJn4POrn.NOceFiARytFC5oQu"); // encrypted value of Asldkdsamw212
		theAppUserStoredInDb.setEmail("old@mail.ru");
		AppUser theNewAppUser = new AppUser();
		theNewAppUser.setPasswordInputField("Asldkdsamw212"); 
		theNewAppUser.setEmail("good@mail.ru");
		theNewAppUser.setUsername("good");
		theNewAppUser.setDisplayName("good");
		
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theNewAppUser, theAppUserStoredInDb);
		mockMvc.perform(post("/user/1").with(csrf()).flashAttr("user", theAppUserStoredInDb))
			.andExpect(view().name("user-profile"))
			.andExpect(model().attributeExists("success"));
		
		verify(userService,times(1)).save(any());
	}
	
	/*
	 * тестирую что пользователь может сменить пароль
	 * 
	 */
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testUserSave_should_be_ok_change_password() throws Exception {
		AppUser theAppUserStoredInDb = new AppUser();
		theAppUserStoredInDb.setPassword("$2a$12$cgnuhz85WrFxgqe2H/n4Su70oG5aaJn4POrn.NOceFiARytFC5oQu"); // encrypted value of Asldkdsamw212
		theAppUserStoredInDb.setEmail("old@mail.ru");
		AppUser theNewAppUser = new AppUser();
		theNewAppUser.setPasswordInputField("Asldkdsamw212"); 
		theNewAppUser.setEmail("old@mail.ru");
		theNewAppUser.setUsername("good");
		theNewAppUser.setDisplayName("good");
		theNewAppUser.setPassword("GoodPassword123");
		theNewAppUser.setPasswordConfirm("GoodPassword123");
		
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theNewAppUser, theAppUserStoredInDb);
		mockMvc.perform(post("/user/1").with(csrf()).flashAttr("user", theAppUserStoredInDb).param("changePassword", "on"))
			.andExpect(view().name("user-profile"))
			.andExpect(model().attributeExists("success"));
		
		verify(userService,times(1)).save(any());
	}

	
	

}



