package ru.vladimir.personalaccounter.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import ru.vladimir.personalaccounter.MockConfiguration;
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.entity.Category;
import ru.vladimir.personalaccounter.entity.Product;
import ru.vladimir.personalaccounter.entity.ProductData;
import ru.vladimir.personalaccounter.entity.PurchaseTransaction;
import ru.vladimir.personalaccounter.entity.Shop;
import ru.vladimir.personalaccounter.enums.TypeOfOperation;
import ru.vladimir.personalaccounter.repository.CategoryRepository;
import ru.vladimir.personalaccounter.repository.ShopRepository;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.ProductService;
import ru.vladimir.personalaccounter.service.PurchaseTransactionService;
import ru.vladimir.personalaccounter.service.UserService;

/**
 *
 * @author vladimir
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = PurchaseTransactionController.class)
@Import(MockConfiguration.class)
public class PurchaseTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private BankAccountService bankAccountService;
    
    
    @MockBean
    private ProductService productService;
    
    @MockBean
    private ShopRepository shopRepository;
    
    @MockBean
    private CategoryRepository categoryRepository;
    
    @MockBean
    private PurchaseTransactionService purchaseTransactionService;
    
	@MockBean
	private UserService userService;
	
	@BeforeEach
	private void setUp() {
		AppUser theUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theUser);

	}
    
   
    public PurchaseTransactionControllerTest() {
    }

    @Test
    @WithMockUser(username = "pupa", roles = "user")
    public void testShouldBeOk() throws Exception {
    	AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
    	BankAccount theTestBankAccount = new BankAccount("test", BigDecimal.valueOf(10), "test bank", theTestUser, Currency.getInstance("RUB"));
    	Shop theShop = new Shop();
    	theShop.setName("5ka");
    	Category theCategory = new Category();
    	theCategory.setName("test");
    	
    	Product theProduct = new Product();
    	theProduct.setName("test");
    	theProduct.setCategory(theCategory);
    	
    	ProductData theProductData = new ProductData();
    	theProductData.setProduct(theProduct);
    	theProductData.setCost(BigDecimal.valueOf(100.2));
    	theProductData.setQuantity(BigDecimal.ONE);
    	
    	PurchaseTransaction purchaseTransaction = new PurchaseTransaction();
    	purchaseTransaction.setAppUser(theTestUser);
    	purchaseTransaction.setBankAccount(theTestBankAccount);
    	purchaseTransaction.setTypeOfOperation(TypeOfOperation.DECREASE);
    	purchaseTransaction.setSumTransaction(theProductData.getCost().multiply(theProductData.getQuantity()));
    	purchaseTransaction.setProductDatas(Collections.singletonList(theProductData));
    	purchaseTransaction.setShop(theShop);
    	
    	mockMvc.perform(post("/purchase-transaction/0").with(csrf()).with(user(theTestUser)).flashAttr("transaction", purchaseTransaction))
    		.andExpect(redirectedUrl("/"));
    	
    	verify(purchaseTransactionService,times(1)).save(purchaseTransaction);
    	
    }
    
    @Test
    @WithMockUser(username = "pupa", roles = " user")
    public void testShouldBeFailValidation() throws Exception {
    	AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
    	BankAccount theTestBankAccount = new BankAccount("test", BigDecimal.valueOf(10), "test bank", theTestUser, Currency.getInstance("RUB"));
    	Shop theShop = new Shop();
    	Category theCategory = new Category();
    	Product theProduct = new Product();
    	theProduct.setCategory(theCategory);
    	ProductData theProductData = new ProductData();
    	theProductData.setProduct(theProduct);
    	PurchaseTransaction thePurchaseTransaction = new PurchaseTransaction();
    	thePurchaseTransaction.setAppUser(theTestUser);
    	thePurchaseTransaction.setBankAccount(theTestBankAccount);
    	thePurchaseTransaction.setProductDatas(Collections.singletonList(theProductData));
    	thePurchaseTransaction.setShop(theShop);
    	
    	mockMvc.perform(post("/purchase-transaction/0").with(csrf()).with(user(theTestUser)).flashAttr("transaction", thePurchaseTransaction))
		.andExpect(model().hasErrors())
		.andExpect(model().attributeHasFieldErrors("transaction",
				"shop.name",
				"productDatas[0].product.category.name",
				"productDatas[0].cost",
				"productDatas[0].quantity",
				"sumTransaction",
				"productDatas[0].product.name"))
		;
    	
    	verify(purchaseTransactionService,times(0)).save(thePurchaseTransaction);
    }

}
