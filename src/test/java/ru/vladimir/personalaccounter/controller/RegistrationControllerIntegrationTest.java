package ru.vladimir.personalaccounter.controller;

import static org.junit.Assert.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.exception.BadAppUserFormatException;
import ru.vladimir.personalaccounter.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@Disabled //он не нужен при каждом запуске, запускать только если меняется логика регистрации.
public class RegistrationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;
    
    public RegistrationControllerIntegrationTest() {
    }

    
    private AppUser theAppUser;
    
    @BeforeEach
    private void createUser() {
    	theAppUser = new AppUser();
    	theAppUser.setUsername("pupa");
    	theAppUser.setDisplayName("pupa");
    	theAppUser.setPassword("aSzXdF7wpBwvZbkK");
    	theAppUser.setPasswordConfirm("aSzXdF7wpBwvZbkK");
    	theAppUser.setEmail("pupamail@mail.ru");
    	
    }
    
    /*
     * тестирую что если пользователь ввел верные параметры - регистрация проходит успешно
     */
    @Test
    public void test_valid_user_should_be_ok() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:8080/login"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.post("http://localhost:8080/registration")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("username", theAppUser.getUsername())
                .param("displayName", theAppUser.getDisplayName())
                .param("email", theAppUser.getEmail())
                .param("password", theAppUser.getPassword())
                .param("passwordConfirm", theAppUser.getPasswordConfirm())
                .flashAttr("newUser", theAppUser)
                .with(csrf())
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().hasNoErrors());
        
        
        
        AppUser userFromDb = userService.findByUsername(theAppUser.getUsername());
        if (userFromDb == null || userFromDb.isEnabled() == true) {
            Assertions.fail();
        }
        
        userService.deleteUser(userFromDb);
    }
    
    /**
     * тест похож на тест из класса /PersonalAccounter/src/test/java/ru/vladimir/personalaccounter/controller/RegistrationControllerTest.java
     * только тут проверяю все на реальной базе данных
     * проверяю что пользователь включен и с ним могу выполнить вход на сервер
     * @throws Exception 
     */
    @Test
    public void test_activateUser_should_be_ok() throws Exception {
    	
    	try {
    		userService.register(theAppUser, Locale.ENGLISH);
    	}
    	catch (BadAppUserFormatException exp){
    		System.out.println("неверный формат заполнения пользователя.");    		
    	}
    	String uuid = theAppUser.getActivationLink().getUuid();
    	mockMvc.perform(get("/registration/" + uuid))
    		.andExpect(redirectedUrl("/login"));
    	
    	AppUser userInDb = userService.findByUsername(theAppUser.getUsername());
    	if (userInDb == null) fail("это не возможно, пользователь не существует");
    	if (!userInDb.isEnabled()) fail("это не возможно, пользователь должен быть активирован");
    	
    	
    	mockMvc.perform(formLogin("/login").user(theAppUser.getUsername()).password(theAppUser.getPassword()))
    		.andExpect(status().is3xxRedirection()); //should redirect to /
    	
    	userService.deleteUser(userInDb);
    }
    

}





