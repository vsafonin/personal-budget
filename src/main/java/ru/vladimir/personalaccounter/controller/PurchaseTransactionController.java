package ru.vladimir.personalaccounter.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.Category;
import ru.vladimir.personalaccounter.entity.Product;
import ru.vladimir.personalaccounter.entity.ProductData;
import ru.vladimir.personalaccounter.entity.PurchaseTransaction;
import ru.vladimir.personalaccounter.entity.Shop;
import ru.vladimir.personalaccounter.exception.PurchaseTransactionNotFound;
import ru.vladimir.personalaccounter.repository.CategoryRepository;
import ru.vladimir.personalaccounter.repository.ShopRepository;
import ru.vladimir.personalaccounter.service.ProductService;
import ru.vladimir.personalaccounter.service.PurchaseTransactionService;
import ru.vladimir.personalaccounter.service.UserService;

/**
 * The Controller for work with PurchaseTransaction entity over web UI.
 * <p>
 * This class handles the CRUD operations for Purchase Transaction entity via HTTP actions
 * as result return HTML based web pages using thymeleaf template.
 * </p>
 * @author Vladimir Afonin
 *
 */
@Controller
public class PurchaseTransactionController {

	@Autowired
	private ProductService productService;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ShopRepository shopRepository;

	@Autowired
	private PurchaseTransactionService purchaseTransactionService;
	
	@Autowired
	private UserService userService;

	/**
	 * Handle /purchase-transaction/{id} requests, return model with Purchase Transaction.
	 * @param id - wanted Purchase Transaction id, if id == 0 add to model new empty Purchase Transaction.
	 * @param model - Model for thymeleaf.
	 * @return - Purchase Transaction edit page.
	 */
	@GetMapping("/purchase-transaction/{id}")
	public String getNewPurchaseTransaction(@PathVariable("id") long id,Model model) {
		PurchaseTransaction purchaseTransaction;
		if (id == 0) {
			AppUser currentUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
			// create new purchase transaction
			purchaseTransaction = new PurchaseTransaction();
			purchaseTransaction.setAppUser(currentUser);
			Product product = new Product();
			ProductData productData = new ProductData();
			productData.setProduct(product);
			List<ProductData> productDataList = Arrays.asList(productData);
			purchaseTransaction.setProductDatas(productDataList);
	}
		else {
			// find purchase transaction in db
			Optional<PurchaseTransaction> purchaseTrnOptional = purchaseTransactionService.findById(id);
			if (purchaseTrnOptional.isPresent()) {
				purchaseTransaction = purchaseTrnOptional.get();
			}
			else {
				throw new PurchaseTransactionNotFound();
			}
		}
		model.addAttribute("transaction", purchaseTransaction);
		return "transaction-purchase-edit";

	}

	/**
	 * Handle /purchase-transaction/delete/{id} requests for deletion PurchaseTransaction from db,
	 * if no error redirect to /.
	 * @param id - PurchaseTransaction id which we want to delete.
	 * @return - if no error redirect to /.
	 */
	@GetMapping("/purchase-transaction/delete/{id}")
	public String deleteTransaction(@PathVariable("id") long id) {
		// get current user
		Optional<PurchaseTransaction> purchaseTrnOptional = purchaseTransactionService.findById(id);
		if (purchaseTrnOptional.isPresent()) {
			purchaseTransactionService.delete(purchaseTrnOptional.get());
			
		}
		else {
			throw new PurchaseTransactionNotFound();
		}
		
		return "redirect:/";

	}
	/**
	 * Handles POST requests /purchase-transaction/{id} for save or create PurchaseTransaction,
	 * if no errors redirects to /
	 * @param id - PurchaseTransaction id, if if == 0 create new PurchaseTransaction, otherwise
	 * save changes.
	 * @param model - Spring Model
	 * @param purchaseTransaction - Purchase Transaction which we want to save
	 * @param bindingResult
	 * @return - if no error redirects to /
	 */
	@PostMapping("/purchase-transaction/{id}")
	public String saveTransaction(@PathVariable("id") long id, Model model,
			@Valid @ModelAttribute("transaction") PurchaseTransaction purchaseTransaction, BindingResult bindingResult) {
		
		if (bindingResult.hasErrors()) {
			return "transaction-purchase-edit";
		}

		
		// check product in DB
		List<ProductData> productDatas = purchaseTransaction.getProductDatas();
		for (ProductData productDataItem : productDatas) {
			// check category
			Optional<Category> categroyOptional = categoryRepository
					.findByName(productDataItem.getProduct().getCategory().getName());
			if (categroyOptional.isPresent()) {
				productDataItem.getProduct().setCategory(categroyOptional.get());
			} else {
				// save it in db
				productDataItem.getProduct().setCategory(categoryRepository.save(productDataItem.getProduct().getCategory()));
			}

			// check product
			Optional<Product> productInDbOptional = productService.findByNameAndCategory(productDataItem.getProduct());
					
			if (productInDbOptional.isPresent()) {
				productDataItem.setProduct(productInDbOptional.get());
			} else {
				// save it
				productDataItem.setProduct(productService.save(productDataItem.getProduct()));
			}

		}

		// check shop in DB
		Optional<Shop> shopOptional = shopRepository.findByName(purchaseTransaction.getShop().getName());
		if (shopOptional.isPresent()) {
			purchaseTransaction.setShop(shopOptional.get());
		} else {
			//save it in db
			shopRepository.save(purchaseTransaction.getShop());
		}
		
		if (purchaseTransaction.getId() == 0) {
			Timestamp currentTime = new Timestamp(System.currentTimeMillis());
			purchaseTransaction.setCreateTime(currentTime);
		}
		
		Currency bankCurrencyOfThisTransaction = purchaseTransaction.getBankAccount().getCurrency(); 
		purchaseTransaction.setCurrency(bankCurrencyOfThisTransaction);
		
		purchaseTransactionService.save(purchaseTransaction);
		return "redirect:/";
	}

	
	/**
	 * Utility method, adds additional line to thymeleaf model
	 * @see https://www.thymeleaf.org/doc/tutorials/2.1/thymeleafspring.html#dynamic-fields
	 * @param model
	 * @param purchaseTransaction - Purchase transaction where we want add additional line
	 * @return - Transaction Purchase edit page.
	 */
	@PostMapping("/purchase-transaction/addLine")
	public String addAdditionalLineToForm(Model model, 
			@ModelAttribute("transaction") PurchaseTransaction purchaseTransaction) {
		// add line
		List<ProductData> productDataList = purchaseTransaction.getProductDatas();
		Product product = new Product();
		ProductData productData = new ProductData();
		productData.setProduct(product);
		if (productDataList == null) {
			productDataList = new ArrayList<ProductData>();
		}
		productDataList.add(productData);		
		purchaseTransaction.setProductDatas(productDataList);
		return "transaction-purchase-edit";
	}
	
	/**
	 * utility method, deletes additional line to thymeleaf model
	 * @see https://www.thymeleaf.org/doc/tutorials/2.1/thymeleafspring.html#dynamic-fields
	 * @param model
	 * @param purchaseTransaction - Purchase transaction from we want del additional line
	 * @return - Transaction Purchase edit page.
	 */
	@PostMapping("/purchase-transaction/delLine/{id}")
	public String delLineFromForm(Model model, @ModelAttribute("transaction") PurchaseTransaction purchaseTransaction,
			@PathVariable("id") int line) {
		
		List<ProductData> currentProductDataList = purchaseTransaction.getProductDatas();
		currentProductDataList.remove(line);
		purchaseTransaction.setProductDatas(currentProductDataList);
		return "transaction-purchase-edit";
	}

	
}
