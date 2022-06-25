package ru.vladimir.personalaccounter.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import ru.vladimir.personalaccounter.MockConfiguration;
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.DebtTransactionService;
import ru.vladimir.personalaccounter.service.PurchaseTransactionService;
import ru.vladimir.personalaccounter.service.SalaryTransactionService;
import ru.vladimir.personalaccounter.service.TransferTransactionService;
import ru.vladimir.personalaccounter.service.UserService;

@WebMvcTest(controllers = {ShowTransactionsController.class, ReportController.class})
@Import(MockConfiguration.class)
@WithAnonymousUser
public class ShowTransactionsControllerTest {
    
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private UserService userService;
	
	@MockBean
	private PurchaseTransactionService purchaseTransactionService;
	
	@MockBean
	private SalaryTransactionService salaryTransactionService;
	
	@MockBean
	private DebtTransactionService debtTransactionService;
	
	@MockBean
	private TransferTransactionService transferTransactionService;
	
	@MockBean
	private BankAccountService bankAccountService;

    /*
     * тестирую что пользователь может открыть страницу с Purchase Transactions
     */
    @Test
    public void testListPurchaseTransactionPageShouldHasBankAccountModel() throws Exception {
    	when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(new AppUser());
    	mockMvc.perform(get("/"))
    		.andExpect(status().isOk())
    		.andExpect(model().attributeExists("bankAccounts"));
    }
    /*
     * тестирую что пользователь может открыть страницу с DebtTransactions
     */
    @Test
    public void testListDebtTransactionPageShouldHasBankAccountModel() throws Exception {
    	when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(new AppUser());
    	mockMvc.perform(get("/debt"))
    	.andExpect(status().isOk())
    	.andExpect(model().attributeExists("bankAccounts"));
    }
    /*
     * тестирую что пользователь может открыть страницу с SalariesTransactions
     */
    @Test
    public void testListSalariesTransactionPageShouldHasBankAccountModel() throws Exception {
    	when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(new AppUser());
    	mockMvc.perform(get("/salary"))
    	.andExpect(status().isOk())
    	.andExpect(model().attributeExists("bankAccounts"));
    }
    /*
     * тестирую что пользователь может открыть страницу с TransferTransactions
     */
    @Test
    public void testListTransferTransactionPageShouldHasBankAccountModel() throws Exception {
    	when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(new AppUser());
    	mockMvc.perform(get("/transfer"))
    	.andExpect(status().isOk())
    	.andExpect(model().attributeExists("bankAccounts"));
    }
    /*
     * тестирую что пользователь может открыть страницу с Report
     */
    @Test
    public void testReportPageShouldHasBankAccountModel() throws Exception {
    	when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(new AppUser());
    	mockMvc.perform(get("/reports"))
    	.andExpect(status().isOk())
    	.andExpect(view().name("report-pie"));
    }
    
}
