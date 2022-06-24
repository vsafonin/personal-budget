package ru.vladimir.personalaccounter.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Disabled;
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
	
	
	
	/**
	 * Тестирую что при пользователь может открыть личный кабинет
	 * @throws Exception 
	 */
	@Disabled //TODO не работает тест, разобраться в причинах
	@Test
	@WithMockUser(roles = "user",username = "pupa")
	void testUserPersonalArea() throws Exception {
		
		//пробуем получить личный кабинет пользователя
		mockMvc.perform(get("/user/1"))
			.andExpect(view().name("user-profile"))
			.andExpect(model().attributeExists("user"));
	}
	
	@Test	
	void testUserPersonalArea_will_be_fail() throws Exception {
			
//		when(userService.findById(Long.parseLong("1"))).thenReturn(null);
		
		//пробуем получить личный кабинет пользователя
		mockMvc.perform(get("/user/1")).andExpect(redirectedUrl("http://localhost/login"));
	}
	
	
	
	
	

}



