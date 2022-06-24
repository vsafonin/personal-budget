package ru.vladimir.personalaccounter.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.methods.JwtProvider;
import ru.vladimir.personalaccounter.repository.ActivationEmailRepository;
import ru.vladimir.personalaccounter.repository.ActivationUuidRepostitory;
import ru.vladimir.personalaccounter.repository.AppUserJwtRepository;
import ru.vladimir.personalaccounter.repository.CategoryRepository;
import ru.vladimir.personalaccounter.repository.PartnerRepository;
import ru.vladimir.personalaccounter.repository.ProductRepository;
import ru.vladimir.personalaccounter.repository.RoleRepository;
import ru.vladimir.personalaccounter.repository.ShopRepository;
import ru.vladimir.personalaccounter.service.AdjustmentTransactionService;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.DebtPaymentService;
import ru.vladimir.personalaccounter.service.DebtTransactionService;
import ru.vladimir.personalaccounter.service.ProductService;
import ru.vladimir.personalaccounter.service.PurchaseTransactionService;
import ru.vladimir.personalaccounter.service.SalaryTransactionService;
import ru.vladimir.personalaccounter.service.UserService;

@ExtendWith(SpringExtension.class)
@WebMvcTest
public class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    
    @MockBean UserDetailsService userDetailsService;

    @MockBean
    private RoleRepository roleRepository;
    
    @MockBean
    private ApplicationEventPublisher eventPublisher;
    
    @MockBean
    private ActivationUuidRepostitory activationUuidRepostitory;
    
    @MockBean
    private BankAccountService bankAccountService;
    
    @MockBean
    private ActivationEmailRepository activationEmailRepository;
    
    @MockBean
    private AdjustmentTransactionService transactionService;
    
    @MockBean
    private PurchaseTransactionService purchaseTransactionService;
    
    @MockBean
    private ProductRepository productRepository;
    
    @MockBean
    private CategoryRepository categoryRepository;
    
    @MockBean
    private ShopRepository shopRepository;
    
    @MockBean
    private SalaryTransactionService salaryTransactionService;

    @MockBean
    private PartnerRepository pathnerRepository;
    
    @MockBean
    private AppUserJwtRepository appUserJwtRepository;
    
    @MockBean
    private JwtProvider jwtProvider;
    
    @MockBean
    private DebtTransactionService debtTransactionService;
    
    @MockBean
    private DebtPaymentService debtPaymentService;
    
    @MockBean
    private ProductService productService;
    
    public RegistrationControllerTest() {
    }

    @Test
    public void test_valid_user_should_be_false_password() throws Exception {
        String name = "pupa";
        String displayname = "pupa";
        String password = "1234"; //will be fail
        String email = "pupa@mail.ru";
        mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:8080/login"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.post("http://localhost:8080/registration")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("username", name)
                .param("displayname", displayname)
                .param("email", email)
                .param("password", password)
                .param("confirm-password", password)
                .flashAttr("newUser", new AppUser())
                .with(csrf())
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().hasErrors());
    }

    @Test
    public void test_valid_user_should_be_fail_password_and_confirm_notMatch() throws Exception {
        AppUser testUser = new AppUser();
        testUser.setUsername("pupa");
        testUser.setDisplayName("pupa");
        testUser.setPassword("aSzXdF7wpBwvZbkK");
        testUser.setPasswordConfirm("213");
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
                .andExpect(MockMvcResultMatchers.model().hasErrors());
    }

    @Test
    public void test_username_exists_should_be_fail() throws Exception {
        String username = "pupa";
        AppUser theUser = new AppUser();
        Mockito.when(userService.findByUsername(username)).thenReturn(theUser);

        mockMvc.perform(MockMvcRequestBuilders.post("http://localhost:8080/registration")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("username", username)
                .param("displayName", username)
                .param("email", "pupa@mail.ru")
                .param("password", "aSzXdF7wpBwvZbkK")
                .param("passwordConfirm", "aSzXdF7wpBwvZbkK")
                .flashAttr("newUser", theUser)
                .with(csrf())
        )
                .andExpect(MockMvcResultMatchers.model().hasErrors());
    }

    @Test
    public void test_username_exists_should_be_ok() throws Exception {
        String username = "pupa";
        AppUser theUser = new AppUser();
        Mockito.when(userService.findByUsername(username)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post("http://localhost:8080/registration")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("username", username)
                .param("displayName", username)
                .param("email", "pupa@mail.ru")
                .param("password", "aSzXdF7wpBwvZbkK")
                .param("passwordConfirm", "aSzXdF7wpBwvZbkK")
                .flashAttr("newUser", theUser)
                .with(csrf())
        )
                .andExpect(MockMvcResultMatchers.model().hasNoErrors());
    }

    @Test
    public void test_email_exists_should_be_fail() throws Exception {
        String email = "pupa@mail.ru";
        AppUser theUser = new AppUser();
        Mockito.when(userService.findByEmail(email)).thenReturn(theUser);

        mockMvc.perform(MockMvcRequestBuilders.post("http://localhost:8080/registration")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("username", "pupa")
                .param("displayName", "pupa")
                .param("email", email)
                .param("password", "aSzXdF7wpBwvZbkK")
                .param("passwordConfirm", "aSzXdF7wpBwvZbkK")
                .flashAttr("newUser", theUser)
                .with(csrf())
        )
                .andExpect(MockMvcResultMatchers.model().hasErrors());
    }

    @Test
    public void test_email_exists_should_be_ok() throws Exception {
        String email = "pupa@mail.ru";
        AppUser theUser = new AppUser();
        Mockito.when(userService.findByEmail(email)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post("http://localhost:8080/registration")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("username", "pupa")
                .param("displayName", "pupa")
                .param("email", email)
                .param("password", "aSzXdF7wpBwvZbkK")
                .param("passwordConfirm", "aSzXdF7wpBwvZbkK")
                .flashAttr("newUser", theUser)
                .with(csrf())
        )
                .andExpect(MockMvcResultMatchers.model().hasNoErrors());
    }
    
    
    /**
     * проверяю что при отправке неверной ссылки uuid 
     * пользователю возвращяется страница заглушка (ошибка) 
     * @throws Exception if wrong address
     */
    @Test
    public void test_user_try_activation_will_be_fail() throws Exception {
    	mockMvc.perform(get("http://localhost:8080/registration/uslak-dallsa-sklal"))
    	.andExpect(view().name("error-activation-page"));
    }
    /**
     * Тут напротив отправляю верную ссылку, жду что будет редирект на основную страницу
     * и пользователь станет активным 
     * @throws Exception if wrong address
     */
    @Test
    public void test_user_try_activation_will_be_ok() throws Exception {
    	String uuid = UUID.randomUUID().toString();
    	AppUser theUser = new AppUser();
    	theUser.setUsername("pupa");
    	theUser.setDisplayName("pupa");
    	theUser.setEmail("pupamail@mail.ru");
    	theUser.setPassword("5z6hQaIVjrwPnTjf");
    	theUser.setPasswordConfirm("5z6hQaIVjrwPnTjf");
    	
    	when(activationUuidRepostitory.findByUuid(uuid)).thenReturn(theUser);
    	when(userService.save(theUser)).thenReturn(theUser);
    	    	
    	
    	mockMvc.perform(get("http://localhost:8080/registration/" + uuid))
    		.andExpect(redirectedUrl("/login"));
    }

}
