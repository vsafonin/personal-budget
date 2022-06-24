package ru.vladimir.personalaccounter.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import ru.vladimir.personalaccounter.MockConfiguration;
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.repository.CategoryRepository;
import ru.vladimir.personalaccounter.repository.ProductRepository;
import ru.vladimir.personalaccounter.repository.RoleRepository;
import ru.vladimir.personalaccounter.repository.ShopRepository;
import ru.vladimir.personalaccounter.service.AdjustmentTransactionService;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.DebtTransactionService;
import ru.vladimir.personalaccounter.service.PurchaseTransactionService;
import ru.vladimir.personalaccounter.service.SalaryTransactionService;
import ru.vladimir.personalaccounter.service.UserService;

@WebMvcTest(controllers = ShowTransactionsController.class)
@Import(MockConfiguration.class)
public class ShowTransactionsControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private RoleRepository roleRepository;
    
    @MockBean
    private BankAccountService bankAccountService;
    
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
    private DebtTransactionService debtTransactionService;
    
	@MockBean
	private UserService userService;
	
	@BeforeEach
	private void setUp() {
		AppUser theUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theUser);

	}
    
    
    public ShowTransactionsControllerTest() {
    }

    /**
     * test that index has bank account
     */
    @Test
 //   @WithMockUser(roles = "user")
    public void testIndexPageShouldHasBankAccountModel() throws Exception {
    	AppUser theUser = new AppUser("pupa","pupa","pass213","pass213","pupamail@gmail.com",true);
    	BankAccount theBankAccount1 = new BankAccount("сбер", BigDecimal.valueOf(100), "" ,theUser, Currency.getInstance("RUB"));
    	BankAccount theBankAccount2 = new BankAccount( "втб", BigDecimal.valueOf(200), "" ,theUser, Currency.getInstance("RUB"));
    	List<BankAccount> accounts = new ArrayList<>();
    	accounts.add(theBankAccount1);
    	accounts.add(theBankAccount2);
    	when(bankAccountService.getBankAccounts()).thenReturn(accounts);
    	
    	mockMvc.perform(get("/").with(user(theUser)))
    		.andExpect(status().isOk())
    		.andExpect(model().attributeExists("bankAccounts"));
    }
    
}
