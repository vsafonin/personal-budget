package ru.vladimir.personalaccounter.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ru.vladimir.personalaccounter.entity.Category;
import ru.vladimir.personalaccounter.entity.DebtTransaction;
import ru.vladimir.personalaccounter.entity.Partner;
import ru.vladimir.personalaccounter.entity.PurchaseTransaction;
import ru.vladimir.personalaccounter.entity.SalaryTransaction;
import ru.vladimir.personalaccounter.entity.Shop;
import ru.vladimir.personalaccounter.entity.TransferTransaction;
import ru.vladimir.personalaccounter.enums.TypeOfOperation;
import ru.vladimir.personalaccounter.service.DebtTransactionService;
import ru.vladimir.personalaccounter.service.PurchaseTransactionService;
import ru.vladimir.personalaccounter.service.SalaryTransactionService;
import ru.vladimir.personalaccounter.service.TransferTransactionService;
/**
 * Controller that shows all transaction lists such as:
 * "/", "/debt","/salary", "/transfer".
 * All pages support page pagination,the limit of elements is set in variable PAGE_LIMIT
 * 
 * @author Vladimir Afonin
 *
 */
@Controller
public class ShowTransactionsController {

	private final int PAGE_LIMIT = 10;
	
	private final int PAGE_NUM_LIMIT = 5;

	@Autowired
	private PurchaseTransactionService purchaseTransactionService;

	@Autowired
	private SalaryTransactionService salaryTransactionService;

	@Autowired
	private DebtTransactionService debtTransactionService;

	
	@Autowired
	private TransferTransactionService transferTransactionService;

	/**
	 * returns HTML page with Purchase list and filter applied 
	 * @param model
	 * @param currentPageNum - current page number, using it for generate pages link in right side and left.
	 * @param bankNameFilter - filter result by bank name
	 * @param shopNameFilter - filter result by shop name
	 * @return HTML page with Purchase list.
	 */
	@GetMapping("/")
	public String getPurchaseTransactions(Model model, @RequestParam(name = "page", defaultValue = "1") int currentPageNum,
			@RequestParam(name = "filterByBankName", defaultValue = "") String bankNameFilter,
			@RequestParam(name = "filterByShop", defaultValue = "") String shopNameFilter,
			@RequestParam(name = "filterByCategory",defaultValue = "") String categoryNameFilter) {
		boolean filterByBank = bankNameFilter != null && !bankNameFilter.isEmpty() && !bankNameFilter.equals("null");
		boolean filterByShop = shopNameFilter != null && !shopNameFilter.isEmpty() && !shopNameFilter.equals("null");
		boolean filterByCategory = categoryNameFilter != null && !categoryNameFilter.isEmpty() && !categoryNameFilter.equals("null");
		model.addAttribute("filterByBank", bankNameFilter);
		model.addAttribute("filterByShop", shopNameFilter);
		model.addAttribute("filterByCategory", categoryNameFilter);
		
		List<PurchaseTransaction> resultListPurchaseTransaction;
		List<PurchaseTransaction> tempListPurchaseTransaction = purchaseTransactionService.getAllPurchaseTransactions();
		Set<Shop> shopList = new HashSet<Shop>();
		tempListPurchaseTransaction.stream().forEach(t -> shopList.add(t.getShop()));
		Set<Category> categorySet = new HashSet<>();
		model.addAttribute("shops", shopList);
		tempListPurchaseTransaction.stream().forEach(t ->
			t.getProductDatas().stream().forEach(pd -> categorySet.add(pd.getProduct().getCategory())));
		model.addAttribute("categories", categorySet);
		
		if(filterByBank) {
			tempListPurchaseTransaction = tempListPurchaseTransaction.stream().filter(t -> t.getBankAccount().getName().equals(bankNameFilter))
					.collect(Collectors.toList());
		}
		
		if (filterByShop) {
			tempListPurchaseTransaction = tempListPurchaseTransaction.stream()
					.filter(t -> t.getShop().getName().equals(shopNameFilter)).collect(Collectors.toList());
		}
		
		if (filterByCategory) {
			tempListPurchaseTransaction = tempListPurchaseTransaction.stream()
					.filter(
							t -> t.getProductDatas()
								.stream().anyMatch(pd -> pd.getProduct().getCategory().getName().equals(categoryNameFilter)))
					.collect(Collectors.toList());
		}
		if (currentPageNum == 1) {

			resultListPurchaseTransaction = tempListPurchaseTransaction.stream().limit(PAGE_LIMIT)
					.collect(Collectors.toList());
		} else {

			resultListPurchaseTransaction = tempListPurchaseTransaction.stream()
					.skip((currentPageNum - 1) * PAGE_LIMIT).limit(PAGE_LIMIT)
					.collect(Collectors.toList());
		}
		model.addAttribute("purchaseTransactions", resultListPurchaseTransaction);

		// add arrays for pagination
		int sizeList = tempListPurchaseTransaction.size();
		model.addAttribute("pagesLeft", getLeftPages(currentPageNum));
		model.addAttribute("pagesRight", getRightPages(currentPageNum, sizeList));
		model.addAttribute("pageNum", currentPageNum);
		model.addAttribute("pageCount", ((sizeList -1)/ PAGE_LIMIT) + 1);

		return "purchase";
	}
	/**
	 * returns HTML page with Transfer transaction list and filter applied 
	 * @param model
	 * @param currentPageNum -  current page number, using it for generate pages link in right side and left.
	 * @param bankNameFilterFrom - filter result by bank name, from which we made the transaction. 
	 * @param bankNameFilterTo  - filter result by bank name  that is the purpose destination of transfer trnasaction
	 * @return HTML page with Transfer transaction list.
	 */
	@GetMapping("/transfer")
	public String getTransferList(Model model, @RequestParam(name = "page", defaultValue = "1") int currentPageNum,
			@RequestParam(name = "filterByBankNameFrom", defaultValue = "") String bankNameFilterFrom,
			@RequestParam(name = "filterByBankNameTo", defaultValue = "") String bankNameFilterTo) {
		boolean filterByBankFrom = bankNameFilterFrom != null && !bankNameFilterFrom.isEmpty() && !bankNameFilterFrom.equals("null");
		boolean filterByBankTo = bankNameFilterTo != null && !bankNameFilterTo.isEmpty() && !bankNameFilterTo.equals("null");
		model.addAttribute("filterByBankFrom", bankNameFilterFrom);
		model.addAttribute("filterByBankTo", bankNameFilterTo);
		List<TransferTransaction> resultList;
		List<TransferTransaction> tempList = transferTransactionService.getTransferTransactions();
		if (filterByBankFrom) {
			tempList = tempList.stream().filter(t -> t.getFromBankAccount().getName().equals(bankNameFilterFrom))
					.collect(Collectors.toList());
		}
		if (filterByBankTo) {
			tempList = tempList.stream().filter(t -> t.getToBankAccount().getName().equals(bankNameFilterTo))
					.collect(Collectors.toList());
		}
		if (currentPageNum == 1) {
			resultList = tempList.stream().limit(PAGE_LIMIT).collect(Collectors.toList());
		}
		else {
			resultList = tempList.stream().skip((currentPageNum - 1) * PAGE_LIMIT).limit(PAGE_LIMIT).collect(Collectors.toList());
		}
		
		int sizeList = tempList.size();
		model.addAttribute("pagesLeft", getLeftPages(currentPageNum));
		model.addAttribute("pagesRight", getRightPages(currentPageNum, sizeList));
		model.addAttribute("pageNum", currentPageNum);
		model.addAttribute("pageCount", sizeList / PAGE_LIMIT > 0 ? (sizeList + 1) / PAGE_LIMIT : 1);
		 
		model.addAttribute("transferTransactions", resultList);
	return "trnsfer-transactions";
	}
	
	/**
	 * returns HTML page with Debt transaction list and filter applied, in one page we show both type of transactions
	 * DECREASE and INCREASE. For filter data we are using different request parameters.  
	 * @param model
	 * @param currentPageNumDecrease - current page number for DECREASE list,
	 * using it for generate pages link in right side and left.
	 * @param currentPageNumIncrease - same as currentPageNumDecrease, but for INCREASE operations
	 * @param bankNameFilter - filter result by bank name
	 * @param partnerNameFilter - filter result by partner name.
	 * @return HTML page with debt two list DECREASE debt transactions and INCREASE debt transactions.
	 */
	@GetMapping("/debt")
	public String getDebt(Model model, @RequestParam(name = "pageDecrease", defaultValue = "1") int currentPageNumDecrease,
			@RequestParam(name = "pageIncrease", defaultValue = "1") int currentPageNumIncrease,
			@RequestParam(name = "filterByBankName", defaultValue = "") String bankNameFilter,
			@RequestParam(name = "filterByPartner", defaultValue = "") String partnerNameFilter) {
		// from thymeleaf i get "null" as string...
		boolean filterByBank = bankNameFilter != null && !bankNameFilter.isEmpty() && !bankNameFilter.equals("null");
		boolean filterPartner = partnerNameFilter != null && !partnerNameFilter.isEmpty()
				&& !partnerNameFilter.equals("null");
		model.addAttribute("filterByBankName", bankNameFilter);
		model.addAttribute("filterByPartner", partnerNameFilter);

		List<DebtTransaction> resultListdebtTransactionDecrease; // for decrease
		List<DebtTransaction> resultListdebtTransactionIncrease; // for increase

		List<DebtTransaction> tempListdebtTransactionDecrease = debtTransactionService.findAll().stream()
				.filter(c -> c.getTypeOfOperation() == TypeOfOperation.DECREASE).filter(c -> c.isActive())
				.collect(Collectors.toList());

		Set<Partner> partnerList = new HashSet<Partner>();
		tempListdebtTransactionDecrease.stream().forEach(p -> partnerList.add(p.getPartner()));

		if (filterByBank) {
			tempListdebtTransactionDecrease = tempListdebtTransactionDecrease.stream()
					.filter(t -> t.getBankAccount().getName().equals(bankNameFilter)).collect(Collectors.toList());
		}

		if (filterPartner) {
			tempListdebtTransactionDecrease = tempListdebtTransactionDecrease.stream()
					.filter(t -> t.getPartner().getName().equals(partnerNameFilter)).collect(Collectors.toList());
		}

		if (currentPageNumDecrease == 1) {
			resultListdebtTransactionDecrease = tempListdebtTransactionDecrease.stream().limit(PAGE_LIMIT)
					.collect(Collectors.toList());
		} else {
			resultListdebtTransactionDecrease = tempListdebtTransactionDecrease.stream()
					.skip((currentPageNumDecrease - 1) * PAGE_LIMIT).limit(PAGE_LIMIT).collect(Collectors.toList());
		}
		model.addAttribute("debtTransactionsDecrease", resultListdebtTransactionDecrease);
		// add arrays for pagination
		int sizeList = tempListdebtTransactionDecrease.size();
		model.addAttribute("pagesLeftDecrease", getLeftPages(currentPageNumDecrease));
		model.addAttribute("pagesRightDecrease", getRightPages(currentPageNumDecrease, sizeList));
		model.addAttribute("pageNumDecrease", currentPageNumDecrease);
		model.addAttribute("pageCountDecrease", sizeList / PAGE_LIMIT > 0 ? (sizeList + 1) / PAGE_LIMIT : 1);

		// increase
		List<DebtTransaction> tempListdebtTransactionIncrease = debtTransactionService.findAll().stream()
				.filter(c -> c.getTypeOfOperation() == TypeOfOperation.INCREASE).filter(c -> c.isActive())
				.collect(Collectors.toList());

		// add partner list to model (for select partner and filter by them)
		tempListdebtTransactionIncrease.stream().forEach(p -> partnerList.add(p.getPartner()));

		if (filterByBank) {
			tempListdebtTransactionIncrease = tempListdebtTransactionIncrease.stream()
					.filter(t -> t.getBankAccount().getName().equals(bankNameFilter)).collect(Collectors.toList());
		}
		if (filterPartner) {
			tempListdebtTransactionIncrease = tempListdebtTransactionIncrease.stream()
					.filter(t -> t.getPartner().getName().equals(partnerNameFilter)).collect(Collectors.toList());
		}
		if (currentPageNumIncrease == 1) {
			resultListdebtTransactionIncrease = tempListdebtTransactionIncrease.stream().limit(PAGE_LIMIT)
					.collect(Collectors.toList());
		} else {
			resultListdebtTransactionIncrease = tempListdebtTransactionIncrease.stream()
					.skip((currentPageNumIncrease - 1) * PAGE_LIMIT).limit(PAGE_LIMIT).collect(Collectors.toList());

		}

		sizeList = tempListdebtTransactionIncrease.size();
		model.addAttribute("pagesLeftIncrease", getLeftPages(currentPageNumIncrease));
		model.addAttribute("pagesRightIncrease", getRightPages(currentPageNumIncrease, sizeList));
		model.addAttribute("pageNumIncrease", currentPageNumIncrease);
		model.addAttribute("pageCountIncrease", sizeList / PAGE_LIMIT > 0 ? (sizeList + 1) / PAGE_LIMIT : 1);

		model.addAttribute("partners", partnerList); // add from decrease and increase

		model.addAttribute("debtTransactionsIncrease", resultListdebtTransactionIncrease);

		return "debt";
	}
	
	/**
	 * returns HTML page with Salary transaction list and filter applied 
	 * @param model
	 * @param currentPageNum - current page number, using it for generate pages link in right side and left.
	 * @param bankNameFilter - filter result data by bank name
	 * @param partnerNameFilter - filter result data by partner name
	 * @return - HTML page with Salary transactions list.
	 */
	@GetMapping("/salary")
	public String getSalary(Model model, @RequestParam(name = "page", defaultValue = "1") int currentPageNum,
			@RequestParam(name = "filterByBankName", defaultValue = "") String bankNameFilter,
			@RequestParam(name = "filterByPartner", defaultValue = "") String partnerNameFilter) {
		// from thymeleaf i get "null" as string...
		boolean filterByBank = bankNameFilter != null && !bankNameFilter.isEmpty() && !bankNameFilter.equals("null");
		boolean filterPartner = partnerNameFilter != null && !partnerNameFilter.isEmpty()
				&& !partnerNameFilter.equals("null");
		model.addAttribute("filterByBankName", bankNameFilter);
		model.addAttribute("filterByPartner", partnerNameFilter);

		model.addAttribute("filterByBank", bankNameFilter);

		List<SalaryTransaction> resultSalaryTransaction;
		List<SalaryTransaction> tempSalaryTransaction = salaryTransactionService.getAllSalaryTransactions();

		// add partner list to model (for select partner and filter by them)
		Set<Partner> partnerList = new HashSet<Partner>();
		tempSalaryTransaction.stream().forEach(p -> partnerList.add(p.getPartner()));
		model.addAttribute("partners", partnerList);
		if (filterByBank) {
			tempSalaryTransaction = tempSalaryTransaction.stream()
					.filter(t -> t.getBankAccount().getName().equals(bankNameFilter)).collect(Collectors.toList());
		}
		if (filterPartner) {
			tempSalaryTransaction = tempSalaryTransaction.stream()
					.filter(t -> t.getPartner().getName().equals(partnerNameFilter)).collect(Collectors.toList());
		}

		if (currentPageNum == 1) {
			resultSalaryTransaction = tempSalaryTransaction.stream().limit(PAGE_LIMIT).collect(Collectors.toList());
		} else {
			resultSalaryTransaction = tempSalaryTransaction.stream().skip((currentPageNum - 1) * PAGE_LIMIT).limit(PAGE_LIMIT)
					.collect(Collectors.toList());
		}
		model.addAttribute("salaryTransactions", resultSalaryTransaction);

		// add arrays for pagination
		int sizeList = tempSalaryTransaction.size();
		model.addAttribute("pagesLeft", getLeftPages(currentPageNum));
		model.addAttribute("pagesRight", getRightPages(currentPageNum, sizeList));
		model.addAttribute("pageNum", currentPageNum);
		model.addAttribute("pageCount", ((sizeList / PAGE_LIMIT) + (sizeList % PAGE_LIMIT)));

		return "salary";
	}
	/**
	 * This method returns array that contains numbers which denotes links on the left side of currentPageNum
	 * First we calculate how many page we are need. For do this we with "for" loop, calculate array size.
	 * This is may be number from 0 to currentPageNum - 5.
	 * Next we add numbers denoting links to page. We want get sutructure like this:
	 * 		currentPageNum = 4
	 *  	result = [1,2,3]
	 *  or
	 *  	currentPageNum = 9
	 *  	result = [4,5,6,7,8]
	 * @param currentPageNum
	 * @return
	 */
	private int[] getLeftPages(int currentPageNum) {
	    int j = 0;
        for (int i = 1; i < currentPageNum && j <= PAGE_NUM_LIMIT; i++, j++)
            ;
        int[] result = new int[j];
        int arraySize = result.length;
        for (int i = 0; i < arraySize; i++) {
            result[(arraySize - 1) - i] = currentPageNum - (i + 1);
        }
        return result;
	}
	
	/**
	 * This method returns arrays with number link. 
	 * first we calculate how many pages there can be. 
	 *	for do this we use this expression :
	 *	int maxCountPage = ((sizeList - 1) /PAGE_LIMIT) - (currentPageNum - 1);
	 * describe:
	 * 	first we calculate how many pages can there be at all.
	 *  we are subtracting 1 from sizeList cause we want get from 10/10 = 0
	 *  we are subtracting 1 from currentPageNum cause it contains from 1 to PAGE_LIMIT positions
	 *   
	 * next we calculate how size array we need. (from 0 to PAGE_NUM_LIMIT), do this with for loop //now is 5
	 * next we are add number of links to new array (we want to get currentPage + i, cause we want get structure like this:
	 * 	currentPageNum = 5
	 * 	result [6,7,8,9,10]
	 *  
	 * @param currentPageNum -current number of page in HTML
	 * @param sizeList - count of elements in List
	 * @return
	 */
	private int[] getRightPages(int currentPageNum, int sizeList) {
		 int maxCountPage = ((sizeList - 1) /PAGE_LIMIT) - (currentPageNum - 1);
	        int j = 0;
	        
	        for (int i = 1; i < maxCountPage + 1 && j <= PAGE_NUM_LIMIT; i++, j++);

	        int[] result = new int[j+1];
	        result[0] = currentPageNum;
	        for (int i = 1; i < result.length; i++) {
	            result[i] = currentPageNum + i;
	        }
	        return result;

	}


}
