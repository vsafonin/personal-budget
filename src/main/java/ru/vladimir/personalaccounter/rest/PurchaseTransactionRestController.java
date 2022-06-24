package ru.vladimir.personalaccounter.rest;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.entity.Category;
import ru.vladimir.personalaccounter.entity.Product;
import ru.vladimir.personalaccounter.entity.ProductData;
import ru.vladimir.personalaccounter.entity.PurchaseTransaction;
import ru.vladimir.personalaccounter.entity.Shop;
import ru.vladimir.personalaccounter.enums.TypeOfOperation;
import ru.vladimir.personalaccounter.exception.BankAccountNotFoundException;
import ru.vladimir.personalaccounter.exception.DefaultBankAccountNotFoundExcp;
import ru.vladimir.personalaccounter.exception.PurchaseTransactionExistsExp;
import ru.vladimir.personalaccounter.repository.CategoryRepository;
import ru.vladimir.personalaccounter.repository.ShopRepository;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.ProductService;
import ru.vladimir.personalaccounter.service.PurchaseTransactionService;
import ru.vladimir.personalaccounter.service.UserService;

@RestController
@RequestMapping(value = "/api")
public class PurchaseTransactionRestController {

	@Autowired
	private PurchaseTransactionService purchaseTransactionService;
	
	@Autowired
	private BankAccountService bankAccountService;
	
	@Autowired
	private ShopRepository shopRepository;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Autowired
	private UserService userService;
	
	
	@GetMapping("/purchase-transaction")
	public List<PurchaseTransaction> getPurchaseTransaction() {
		List<PurchaseTransaction> result = purchaseTransactionService.getAllPurchaseTransactions().stream().limit(20).collect(Collectors.toList()); 
		return result;
	}
	
	@PostMapping("/purchase-transaction")
	public PurchaseTransaction savePurchaseTransaction(@RequestBody PurchaseTransaction theNewPurchaseTransaction) {
		if (theNewPurchaseTransaction.isFromJson()) {
			//find by fiscal signt
			Optional<PurchaseTransaction> purchaseTransactionInDbOptional = purchaseTransactionService.findByFiscalSign(theNewPurchaseTransaction.getFiscalSign());
			if (purchaseTransactionInDbOptional.isPresent()) {
				throw new PurchaseTransactionExistsExp();
			}
		}
		//add appUser
		AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		theNewPurchaseTransaction.setAppUser(theAppUser);
		theNewPurchaseTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
		theNewPurchaseTransaction.setTypeOfOperation(TypeOfOperation.DECREASE);

		//get bank account
		BankAccount theBankAccount = theNewPurchaseTransaction.getBankAccount();
		if (theBankAccount == null) {
			theBankAccount = bankAccountService.getBankAccounts().stream().filter(ba -> ba.isDefaultAccount()).findAny().orElse(null);
			if (theBankAccount == null) {
				throw new DefaultBankAccountNotFoundExcp("user not set default bank account!!");
			}
		}
		else {
			theBankAccount = bankAccountService.getBankAccounts().stream()
					.filter(ba -> ba.getName().equals(theNewPurchaseTransaction.getBankAccount().getName()))
							.findFirst().orElse(null);
			if (theBankAccount == null) {
				throw new BankAccountNotFoundException(null);
			}
		}
		theNewPurchaseTransaction.setBankAccount(theBankAccount);
		theNewPurchaseTransaction.setCurrency(theNewPurchaseTransaction.getBankAccount().getCurrency());
		
		//get shop
		if (theNewPurchaseTransaction.getShop() == null) {
			throw new IllegalArgumentException("Shop is null");
		}
		else {
			Optional<Shop> theShopOptional = shopRepository.findByName(theNewPurchaseTransaction.getShop().getName());
			if (theShopOptional.isPresent()) {
				theNewPurchaseTransaction.setShop(theShopOptional.get());
			}
			else {
				Shop theNewShop = theNewPurchaseTransaction.getShop();
				theNewPurchaseTransaction.setShop(shopRepository.save(theNewShop));
			}
		}
		
		//create product datas
		if (theNewPurchaseTransaction.getProductDatas() == null || theNewPurchaseTransaction.getProductDatas().size() == 0) {
			throw new IllegalArgumentException("Product data is null");
		}
		/**
		 * 1. check category present in db? if not create new
		 * 2. check product name present in db? if not create new
		 * set this element in Product data
		 */
		for (ProductData pd : theNewPurchaseTransaction.getProductDatas()) {
			if (pd.getProduct() == null || pd.getProduct().getName() == null || pd.getProduct().getName().isBlank()) {
				throw new IllegalArgumentException("Product is null");
			}
			if (pd.getProduct().getCategory() == null) {

				Category theCategory = new Category();
				theCategory.setName("not specified"); //TODO change to internalization
				pd.getProduct().setCategory(theCategory);
			}
			Optional<Category> theCategoryOptional = categoryRepository.findByName(pd.getProduct().getCategory().getName());
			if (theCategoryOptional.isPresent()) {
				pd.getProduct().setCategory(theCategoryOptional.get());
			}
			else {
				pd.getProduct().setCategory(categoryRepository.save(pd.getProduct().getCategory()));
			}
			
			Optional<Product> theProductOptional = productService.findByName(pd.getProduct());
					if (theProductOptional.isPresent()) {
						pd.setProduct(theProductOptional.get());
					}
					else {
						pd.setProduct(productService.save(pd.getProduct()));
					}
		}
		
		return purchaseTransactionService.save(theNewPurchaseTransaction);
	}
}
