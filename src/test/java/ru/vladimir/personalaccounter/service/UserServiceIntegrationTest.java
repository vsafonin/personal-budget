package ru.vladimir.personalaccounter.service;

import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.exception.BadAppUserFormatException;

@SpringBootTest
public class UserServiceIntegrationTest {


	@Autowired
	private UserService userService;

    
    @Test
    @Disabled
    public void test_register_user_should_be_ok(){
        AppUser theUser = new AppUser();
        theUser.setUsername("pupaTest");
        theUser.setEmail("pupaTest@gmail.com");
        theUser.setPassword("1fEQc05BYdf3nf8z");
        theUser.setPasswordConfirm("1fEQc05BYdf3nf8z");
        theUser.setDisplayName("pupaTest");
        
        try {
            userService.register(theUser, Locale.ENGLISH);
        }
        catch (BadAppUserFormatException exp) {
            Assertions.fail("bad user exception");
        }
        
        AppUser userInDb = userService.findByUsername(theUser.getUsername());
        if (userInDb == null) Assertions.fail("user not store in db");
        if (userInDb.isEnabled()) Assertions.fail("user is enabled (by default he must be disabled)");
        if (userInDb.getCreateTime() == null) Assertions.fail("user doesn't have creation time");
        
        //delete user
        userService.deleteUser(theUser);
    }
}
