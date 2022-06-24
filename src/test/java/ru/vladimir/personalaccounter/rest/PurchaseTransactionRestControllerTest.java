package ru.vladimir.personalaccounter.rest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.vladimir.personalaccounter.MockConfiguration;
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.Authorities;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.entity.Category;
import ru.vladimir.personalaccounter.entity.Product;
import ru.vladimir.personalaccounter.entity.ProductData;
import ru.vladimir.personalaccounter.entity.PurchaseTransaction;
import ru.vladimir.personalaccounter.entity.Shop;
import ru.vladimir.personalaccounter.enums.AauthoritiesEnum;
import ru.vladimir.personalaccounter.methods.JwtProvider;
import ru.vladimir.personalaccounter.repository.AppUserJwtRepository;
import ru.vladimir.personalaccounter.repository.CategoryRepository;
import ru.vladimir.personalaccounter.repository.ProductRepository;
import ru.vladimir.personalaccounter.repository.ShopRepository;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.ProductService;
import ru.vladimir.personalaccounter.service.PurchaseTransactionService;
import ru.vladimir.personalaccounter.service.UserService;
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = PurchaseTransactionRestController.class)
@Import(MockConfiguration.class)
class PurchaseTransactionRestControllerTest {
	
	private final static String TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0IiwiZXhwIjoxNjYyNDk0NDAwfQ.eNso11oEG-bwgIfV81cQH78QzvSp1bdhvXDCOSq9RHklWAkQulQ3-dhBc5Bathd8KLxELW2KurD1CqWYwkWXSA";
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private AppUserJwtRepository appUserJwtRepository;
	
	@MockBean
	private JwtProvider jwtProvider;
	
	@MockBean
	private UserDetailsService userDetailsService;
	
	@MockBean
	private BankAccountService bankAccountService;
	
	@MockBean
	private PurchaseTransactionService purchaseTransactionService;
	
	@MockBean
	private ShopRepository shopRepository;
	
	@MockBean
	private ProductRepository productRepository;
	
	@MockBean
	private CategoryRepository categoryRepository;
	
	@MockBean
	private ProductService productService;
	
	@Mock
    private Authentication auth;
	
	@MockBean
	private UserService userService;
	
	
	@Autowired
	private ObjectMapper mapper;
	
	private AppUser theAppUser;
	
	@BeforeEach
	void setUp() {
		theAppUser = new AppUser("test", "test", "123", "123", "pupa@mail.ru", true);
		theAppUser.setRoles(Set.of(new Authorities(AauthoritiesEnum.USER.getRoleName())));
		when(auth.getPrincipal()).thenReturn(theAppUser);
        SecurityContextHolder.getContext().setAuthentication(auth);
	}
	
	
	@Test
	void testGetpurchaseTransactionshouldBeFail() throws Exception {
		mockMvc.perform(get("/api/purchase-transaction").header(HttpHeaders.AUTHORIZATION, "Bearer " + "ss")).andExpect(status().isUnauthorized());
	}
	
	@Test
	void testGetpurchaseTransactionshouldBeOk() throws Exception {
		when(jwtProvider.validateToken(TOKEN)).thenReturn(true);
		when(jwtProvider.getLoginFromToken(TOKEN)).thenReturn("test");
		when(userDetailsService.loadUserByUsername("test")).thenReturn(theAppUser);

		when(purchaseTransactionService.getAllPurchaseTransactions()).thenReturn(new ArrayList<>());
		mockMvc.perform(get("/api/purchase-transaction").header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)).andExpect(status().isOk());
	}

	@Test
	void testSavePurchaseTransaction() throws Exception, Exception {
		when(jwtProvider.validateToken(TOKEN)).thenReturn(true);
		when(jwtProvider.getLoginFromToken(TOKEN)).thenReturn("test");
		when(userDetailsService.loadUserByUsername("test")).thenReturn(theAppUser);
		
		BankAccount theBankAccount = new BankAccount();
		theBankAccount.setName("test");
		when(bankAccountService.getBankAccounts()).thenReturn(List.of(theBankAccount));
		
		Shop theShop = new Shop();
		theShop.setName("test");
		when(shopRepository.findByName("test")).thenReturn(Optional.of(theShop));
		
		
		ProductData theProductData = new ProductData();
		Category theCategory = new Category();
		theCategory.setName("test");
		Product theProduct = new Product();
		theProduct.setName("test");
		theProduct.setCategory(theCategory);
		theProductData.setProduct(theProduct);
		when(categoryRepository.findByName("test")).thenReturn(Optional.of(theCategory));
		when(productRepository.findByNameAndCategory("test",theCategory)).thenReturn(Optional.of(theProduct));
		PurchaseTransaction thePurchaseTransaction = new PurchaseTransaction();
		thePurchaseTransaction.setBankAccount(theBankAccount);
		thePurchaseTransaction.setProductDatas(List.of(theProductData));
		thePurchaseTransaction.setShop(theShop);
		when(purchaseTransactionService.save(any(PurchaseTransaction.class))).thenReturn(thePurchaseTransaction);
		//test
		
		mockMvc.perform(post("/api/purchase-transaction").header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
				.content(mapper.writeValueAsString(thePurchaseTransaction)).contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON));
		
		
	}
	@Test
	void testSavepurchaseTransactionshouldBeOkWithoutBank() throws Exception, Exception {
		when(jwtProvider.validateToken(TOKEN)).thenReturn(true);
		when(jwtProvider.getLoginFromToken(TOKEN)).thenReturn("test");
		when(userDetailsService.loadUserByUsername("test")).thenReturn(theAppUser);
		
		BankAccount theBankAccount = new BankAccount();
		theBankAccount.setName("test");
		theBankAccount.setDefaultAccount(true);
		when(bankAccountService.getBankAccounts()).thenReturn(List.of(theBankAccount));
		
		Shop theShop = new Shop();
		theShop.setName("test");
		when(shopRepository.findByName("test")).thenReturn(Optional.of(theShop));
		
		
		ProductData theProductData = new ProductData();
		Category theCategory = new Category();
		theCategory.setName("test");
		Product theProduct = new Product();
		theProduct.setName("test");
		theProduct.setCategory(theCategory);
		theProductData.setProduct(theProduct);
		when(categoryRepository.findByName("test")).thenReturn(Optional.of(theCategory));
		when(productRepository.findByNameAndCategory("test",theCategory)).thenReturn(Optional.of(theProduct));
		PurchaseTransaction thePurchaseTransaction = new PurchaseTransaction();
		thePurchaseTransaction.setProductDatas(List.of(theProductData));
		thePurchaseTransaction.setShop(theShop);
		when(purchaseTransactionService.save(any(PurchaseTransaction.class))).thenReturn(thePurchaseTransaction);
		//test
		
		mockMvc.perform(post("/api/purchase-transaction").header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
				.content(mapper.writeValueAsString(thePurchaseTransaction)).contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON));	
		
		
		
	}
	@Test
	void testSavepurchaseTransactionshouldBeFailWithouShop() throws Exception, Exception {
		when(jwtProvider.validateToken(TOKEN)).thenReturn(true);
		when(jwtProvider.getLoginFromToken(TOKEN)).thenReturn("test");
		when(userDetailsService.loadUserByUsername("test")).thenReturn(theAppUser);
		
		BankAccount theBankAccount = new BankAccount();
		theBankAccount.setName("test");
		theBankAccount.setDefaultAccount(true);
		when(bankAccountService.getBankAccounts()).thenReturn(List.of(theBankAccount));
		
//		Shop theShop = new Shop();
//		theShop.setName("test");
//		when(shopRepository.findByName("test")).thenReturn(Optional.of(theShop));
		
		
		ProductData theProductData = new ProductData();
		Category theCategory = new Category();
		theCategory.setName("test");
		Product theProduct = new Product();
		theProduct.setName("test");
		theProduct.setCategory(theCategory);
		theProductData.setProduct(theProduct);
		when(categoryRepository.findByName("test")).thenReturn(Optional.of(theCategory));
		when(productRepository.findByNameAndCategory("test",theCategory)).thenReturn(Optional.of(theProduct));
		PurchaseTransaction thePurchaseTransaction = new PurchaseTransaction();
		thePurchaseTransaction.setProductDatas(List.of(theProductData));
		when(purchaseTransactionService.save(any(PurchaseTransaction.class))).thenReturn(thePurchaseTransaction);
		//test
		
		mockMvc.perform(post("/api/purchase-transaction").header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
				.content(mapper.writeValueAsString(thePurchaseTransaction)).contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isUnprocessableEntity())
		.andExpect(content().string("Shop is null"));
		
	}
	@Test
	void testSavePurchaseTransactionshouldBeFailWithouProduct() throws Exception, Exception {
		when(jwtProvider.validateToken(TOKEN)).thenReturn(true);
		when(jwtProvider.getLoginFromToken(TOKEN)).thenReturn("test");
		when(userDetailsService.loadUserByUsername("test")).thenReturn(theAppUser);
		
		BankAccount theBankAccount = new BankAccount();
		theBankAccount.setName("test");
		theBankAccount.setDefaultAccount(true);
		when(bankAccountService.getBankAccounts()).thenReturn(List.of(theBankAccount));
		
		Shop theShop = new Shop();
		theShop.setName("test");
		when(shopRepository.findByName("test")).thenReturn(Optional.of(theShop));
		
		
		ProductData theProductData = new ProductData();
//		Category theCategory = new Category();
//		theCategory.setName("test");
		Product theProduct = new Product();
//		theProduct.setName("test");
//		theProduct.setCategory(theCategory);
		theProductData.setProduct(theProduct);
//		when(categoryRepository.findByName("test")).thenReturn(Optional.of(theCategory));
//		when(productRepository.findByName("test")).thenReturn(Optional.of(theProduct));
		PurchaseTransaction thePurchaseTransaction = new PurchaseTransaction();
//		theShopTransaction.setBankAccount(theBankAccount);
		thePurchaseTransaction.setProductDatas(List.of(theProductData));
		thePurchaseTransaction.setShop(theShop);
		when(purchaseTransactionService.save(any(PurchaseTransaction.class))).thenReturn(thePurchaseTransaction);
		//test
		
		mockMvc.perform(post("/api/purchase-transaction").header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
				.content(mapper.writeValueAsString(thePurchaseTransaction)).contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isUnprocessableEntity())
		.andExpect(content().string("Product is null"));
		
	}

}
