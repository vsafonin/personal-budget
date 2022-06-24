package ru.vladimir.personalaccounter.controller;

import static org.junit.Assert.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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
import ru.vladimir.personalaccounter.repository.UserRepository;
import ru.vladimir.personalaccounter.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
public class RegistrationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository repository;
    
    public RegistrationControllerIntegrationTest() {
    }
    @AfterEach
    public void removeUserFromDb() {
    	List<AppUser> users = repository.findAll();
    	users.stream().forEach(u -> userService.deleteUser(u));
    }
    
    @Test
    public void test_valid_user_should_be_ok() throws Exception {
        AppUser testUser = new AppUser();
        testUser.setUsername("pupa");
        testUser.setDisplayName("pupa");
        testUser.setPassword("aSzXdF7wpBwvZbkK");
        testUser.setPasswordConfirm("aSzXdF7wpBwvZbkK");
        testUser.setEmail("pupamail@mail.ru");

        mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:8080/login"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.post("http://localhost:8080/registration")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("username", testUser.getUsername())
                .param("displayName", testUser.getDisplayName())
                .param("email", testUser.getEmail())
                .param("password", testUser.getPassword())
                .param("passwordConfirm", testUser.getPasswordConfirm())
                .flashAttr("newUser", testUser)
                .with(csrf())
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().hasNoErrors());
        
        
        
        AppUser userFromDb = userService.findByUsername(testUser.getUsername());
        if (userFromDb == null || userFromDb.isEnabled() == true) {
            Assertions.fail();
        }
        
        //delete user
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
    	String password = "LIpHVscoXmMWAsHS";
    	AppUser theUser = new AppUser();
    	theUser.setUsername("pupa");
    	theUser.setDisplayName("pupa");
    	theUser.setPassword(password);
    	theUser.setPasswordConfirm(password);
    	theUser.setEmail("vsafonin@gmail.com");
    	
    	try {
    		userService.register(theUser, Locale.ENGLISH);
    	}
    	catch (BadAppUserFormatException exp){
    		System.out.println("неверный формат заполнения пользователя.");    		
    	}
    	String uuid = theUser.getActivationLink().getUuid();
    	mockMvc.perform(get("http://localhost:8080/registration/" + uuid))
    		.andExpect(redirectedUrl("/login"));
    	
    	AppUser userInDb = userService.findByUsername(theUser.getUsername());
    	if (userInDb == null) fail("это не возможно, пользователь не существует");
    	if (!userInDb.isEnabled()) fail("это не возможно, пользователь должен быть активирован");
    	
    	
    	mockMvc.perform(formLogin("/login").user(theUser.getUsername()).password(password))
    		.andExpect(status().is3xxRedirection()); //should redirect to /
    }
    

}





