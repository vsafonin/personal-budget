package ru.vladimir.personalaccounter.service;

import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.Authorities;
import ru.vladimir.personalaccounter.enums.AauthoritiesEnum;
import ru.vladimir.personalaccounter.event.EmailEvent;
import ru.vladimir.personalaccounter.exception.BadAppUserFormatException;
import ru.vladimir.personalaccounter.repository.ActivationUuidRepostitory;
import ru.vladimir.personalaccounter.repository.RoleRepository;
import ru.vladimir.personalaccounter.repository.UserRepository;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @MockBean
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;
    
    @MockBean
    private PasswordEncoder passwordEncoder;
    
    @MockBean 
    private RoleRepository roleRepository;

    @MockBean
    private ApplicationEventPublisher eventPublisher;

    @MockBean
    private ActivationUuidRepostitory activationLinksRepostitory;

    public UserServiceImplTest() {
    }

    @Test
    public void testRegisterMethod() {
        AppUser theUser = new AppUser();
        theUser.setDisplayName("pupa");
        theUser.setEmail("pupamail@mail.ru");
        theUser.setPassword("daSkJjsmn2lsma");
        theUser.setPasswordConfirm("daSkJjsmn2lsma");
        theUser.setUsername("pupa");
        
        Authorities userRole = new Authorities();
        userRole.setName(AauthoritiesEnum.USER.getRoleName());

        Mockito.when(userRepository.save(theUser)).thenReturn(theUser);
        Mockito.when(roleRepository.findByName("user")).thenReturn(userRole);
        try {
            userService.register(theUser, Locale.ENGLISH);
        }
        catch (BadAppUserFormatException exp) {
            Assertions.fail();
        }

        Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(ArgumentMatchers.any(EmailEvent.class));
        
    }
    
    @Test
    public void testRegisterMethod_should_be_fail_email_not_valid() {
        AppUser theUser = new AppUser();
        theUser.setDisplayName("pupa");
        theUser.setEmail("");
        theUser.setPassword("daSkJjsmn2lsma");
        theUser.setPasswordConfirm("daSkJjsmn2lsma");
        theUser.setUsername("pupa");

        Assertions.assertThrows(BadAppUserFormatException.class, 
                
                () -> { 
                    userService.register(theUser, Locale.ENGLISH);
                            });
        
        Mockito.verify(eventPublisher, Mockito.times(0)).publishEvent(ArgumentMatchers.any(EmailEvent.class));

    }
    @Test
    public void testRegisterMethod_should_be_fail_email_not_valid_null() {
        AppUser theUser = new AppUser();
        theUser.setDisplayName("pupa");
        theUser.setEmail(null);
        theUser.setPassword("daSkJjsmn2lsma");
        theUser.setPasswordConfirm("daSkJjsmn2lsma");
        theUser.setUsername("pupa");

        Assertions.assertThrows(BadAppUserFormatException.class, 
                
                () -> { 
                    userService.register(theUser, Locale.ENGLISH);
                            });
        
        Mockito.verify(eventPublisher, Mockito.times(0)).publishEvent(ArgumentMatchers.any(EmailEvent.class));

    }
    @Test
    public void testRegisterMethod_should_be_fail_user_not_valid_null() {
       Assertions.assertThrows(BadAppUserFormatException.class, 
                
                () -> { 
                    userService.register(null, Locale.ENGLISH);
                            });
        
        Mockito.verify(eventPublisher, Mockito.times(0)).publishEvent(ArgumentMatchers.any(EmailEvent.class));

    }
    
        @Test
    public void testRegisterMethod_should_be_fail_user_not_valid_empty() {
     Assertions.assertThrows(BadAppUserFormatException.class, 
                
                () -> { 
                    userService.register(new AppUser(), Locale.ENGLISH);
                            });
        
        Mockito.verify(eventPublisher, Mockito.times(0)).publishEvent(ArgumentMatchers.any(EmailEvent.class));

    }
        
}
