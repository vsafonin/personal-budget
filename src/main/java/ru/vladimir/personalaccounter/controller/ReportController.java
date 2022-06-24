package ru.vladimir.personalaccounter.controller;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import ru.vladimir.personalaccounter.entity.ProductData;
import ru.vladimir.personalaccounter.entity.PurchaseTransaction;
import ru.vladimir.personalaccounter.entity.SalaryTransaction;
import ru.vladimir.personalaccounter.enums.TypeOfReport;
import ru.vladimir.personalaccounter.model.ReportModel;
import ru.vladimir.personalaccounter.service.PurchaseTransactionService;
import ru.vladimir.personalaccounter.service.SalaryTransactionService;
/**
 * Controller handle request for /reports. 
 *<p>
 * If request is GET - returns report-pie page with default settings.
 * Default is most used categories on purchase transactions
 * If request is POST - we are checking parameter in @ModelAttribute ReportModel and
 * return required report with data.
 * 
 *</p>
 * @author Vladimir Afonin
 *
 */
@Controller
public class ReportController {
	
	@Autowired
	private PurchaseTransactionService purchaseTransactionService;
	
	@Autowired
	private SalaryTransactionService salaryTransactionService;
	
	@Autowired
	private MessageSource messageSource;
	
	
	@ModelAttribute
	private void addNeccesaryAttr(Model model) {
		model.addAttribute("typeOfReports", TypeOfReport.values());
	}
	
	/**
	 * Handles POST request to /reports, return model with required data.
	 * Next in model this data will be transformed to diagram.
	 * @param reportModel - ReportModel object, if null this method returns report page by default
	 * @param model
	 * @param locale
	 * @return - HTML page with data for generate diagram. 
	 */
	@PostMapping("/reports")
	private String selectReport(@ModelAttribute("reportModel") 
		ReportModel reportModel, Model model, Locale locale) {
		if (reportModel == null) {
			getReport(model, locale);
		}
		//what date does user ask for
		if (reportModel.getStartDate() == null)
			reportModel.setStartDate(getFirstDayMonth()); //First of the month
		if (reportModel.getEndDate() == null) 
			reportModel.setEndDate(new Date()); //now

		//check end and start date
		if (reportModel.getStartDate().after(reportModel.getEndDate())) {
			String localizedErrorMessage = messageSource.getMessage("reports.pie.dateEndOlderThenStart", null, locale);
			model.addAttribute("errorDate", localizedErrorMessage);
			model.addAttribute("chartData", new ArrayList<List<Object>>());
			return "report-pie";
		}
		
		StringBuilder reportTitleStr = new StringBuilder();
		
		if (reportModel.getTypeOfReport() == TypeOfReport.PURCHASE_CATEGORY) {
		
			model.addAttribute("chartData",getPurchaseTransactionOrderedByCategory(reportModel.getStartDate(), reportModel.getEndDate()));
			String localizedTitleStr = messageSource.getMessage("reports.pie.categories", null, locale); 
			reportTitleStr.append(localizedTitleStr);
			
		}
		
		else if (reportModel.getTypeOfReport() == TypeOfReport.PURCHASE_PRODUCT) {
			model.addAttribute("chartData",getPurchaseTransactionOrderedByProduct(reportModel.getStartDate(), reportModel.getEndDate()));
			String localizedTitleStr = messageSource.getMessage("reports.pie.product", null, locale);
			reportTitleStr.append(localizedTitleStr);
		}
		
		else if (reportModel.getTypeOfReport() == TypeOfReport.SALARY_INCREASE) {
			model.addAttribute("chartData",getSalaryTransactionOrderedByPartner(reportModel.getStartDate(), reportModel.getEndDate()));
			String localizedTitleStr = messageSource.getMessage("reports.pie.salary", null, locale);
			reportTitleStr.append(localizedTitleStr);
		}
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
		String reportStartDate = simpleDateFormat.format(reportModel.getStartDate()); 
		String reportEndDate = simpleDateFormat.format(reportModel.getEndDate()); 
		
		reportTitleStr.append(" ").append(messageSource.getMessage("reports.pie.between", null, locale))
		.append(" ")
		.append(reportStartDate)
		.append(" ")
		.append(messageSource.getMessage("reports.pie.and", null, locale))
		.append(" ")
		.append(reportEndDate);
		
		
		
		model.addAttribute("reportTitle", reportTitleStr.toString());
		
		model.addAttribute("reportModel",reportModel);
		
		return "report-pie";
	}


	/**
	 * Transforms SalaryTransaction list to format with which can work into Google charts. 
	 * This is simple format,for example result must be look like this: List<List<Object>> list
	 * Object must be look like this: String, Value
	 * To collect all records of one single object in a given period and calculate the total amount,
	 * getting list from DB and putting Partner Name and Sum Transaction into map. 
	 * Next add this MAP to List<List<Object>> result
	 * @param startDate - Start date for fetching from DB.
	 * @param endDate - End date for fetching from DB
	 * @return - List<List<Object>> Object 
	 */
	private Object getSalaryTransactionOrderedByPartner(Date startDate, Date endDate) {
		List<SalaryTransaction> salaryTransactionsInDateRange = salaryTransactionService.getAllSalaryTransactions().stream()
				.filter(st -> st.getCreateTime().after(startDate) && st.getCreateTime().before(addOneDay(endDate)))
				.collect(Collectors.toList());

		Map<String, BigDecimal> results = new HashMap<>();
		for (SalaryTransaction st : salaryTransactionsInDateRange) {
			String partnerName = st.getPartner().getName();
			if (results.containsKey(partnerName)) {
				results.put(partnerName, results.get(partnerName).add(st.getSumTransaction()));
			} else {
				results.put(partnerName, st.getSumTransaction());
			}
		}
		List<List<Object>> result = new ArrayList<List<Object>>();
		for (Entry<String, BigDecimal> entry : results.entrySet()) {
			result.add(List.of(entry.getKey(), entry.getValue()));
		}

		return result;
	}

	/**
	 * Transforms Purchase Transaction list to format with which can work into Google charts. 
	 * This is simple format,for example result must be look like this: List<List<Object>> list
	 * Object must be look like this: String, Value
	 * To collect all records of one single object in a given period and calculate the total amount,
	 * getting list from DB and getting Category Name and calculating sum from cost and quantity and putting it into MAP. 
	 * Next add this MAP to List<List<Object>> result
	 * @param startDate - Start date for fetching from DB.
	 * @param endDate - End date for fetching from DB
	 * @return - List<List<Object>> Object 
	 */
	private Object getPurchaseTransactionOrderedByCategory(Date startDate, Date endDate) {
		List<PurchaseTransaction> purchaseTransactionInDateRange = purchaseTransactionService.getAllPurchaseTransactions().stream()
				.filter(st -> st.getCreateTime().after(startDate) && st.getCreateTime().before(addOneDay(endDate)) )
				.collect(Collectors.toList());
		
		Map<String, BigDecimal> results = new HashMap<>();
		for (PurchaseTransaction st: purchaseTransactionInDateRange) {
			for (ProductData pd: st.getProductDatas()) {
				String categoryName = pd.getProduct().getCategory().getName();
				if (results.containsKey(categoryName)) {
					BigDecimal sum = pd.getCost().multiply(pd.getQuantity());
					BigDecimal totalSumInMap = results.get(categoryName);
					results.put(categoryName, totalSumInMap.add(sum));
				}
				else {
					BigDecimal sum = pd.getCost().multiply(pd.getQuantity());
					results.put(categoryName, sum);
				}
			}
		}
		
		List<List<Object>> result = new ArrayList<List<Object>>();
		for (Entry<String, BigDecimal> entry: results.entrySet()) {
			result.add(
					List.of(entry.getKey(),entry.getValue())
					);
		}

		return result;
	}
	
	/**
	 * Transforms Purchase Transaction list to format with which can work into Google charts. 
	 * This is simple format,for example result must be look like this: List<List<Object>> list
	 * Object must be look like this: String, Value
	 * To collect all records of one single object in a given period and calculate the total amount,
	 * getting list from DB and getting Product Name and calculating sum from cost and quantity and putting it into MAP. 
	 * Next add this MAP to List<List<Object>> result
	 * @param startDate - Start date for fetching from DB.
	 * @param endDate - End date for fetching from DB
	 * @return - List<List<Object>> Object 
	 */
	private Object getPurchaseTransactionOrderedByProduct(Date startDate, Date endDate) {
		
		List<PurchaseTransaction> purchaseTransactions = purchaseTransactionService.getAllPurchaseTransactions().stream()
				.filter(pt -> pt.getCreateTime().after(startDate) && pt.getCreateTime().before(addOneDay(endDate)) )
				.collect(Collectors.toList());
		
		Map<String, BigDecimal> results = new HashMap<>();
		for (PurchaseTransaction pt: purchaseTransactions) {
			for (ProductData pd: pt.getProductDatas()) {
				String productName = pd.getProduct().getName();
				if (results.containsKey(productName)) {
					BigDecimal sum = pd.getCost().multiply(pd.getQuantity());
					BigDecimal totalSumInMap = results.get(productName);
					results.put(productName, totalSumInMap.add(sum));
				}
				else {
					BigDecimal sum = pd.getCost().multiply(pd.getQuantity());
					results.put(productName, sum);
				}
			}
		}
		List<List<Object>> result = new ArrayList<List<Object>>();
		for (Entry<String, BigDecimal> entry: results.entrySet()) {
			result.add(
					List.of(entry.getKey(),entry.getValue())
					);
		}
		
		return result;
	}

	/**
	 * adds One day to original date
	 * @param date - original Date
	 * @return - original date + one day. 
	 */
	private Date addOneDay(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, 1);
		return c.getTime();
	}
	/**
	 * returns first day of this month
	 * @return - first day of this month
	 */
	private Date getFirstDayMonth() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Timestamp mounthBegin = new Timestamp(cal.getTime().getTime());
		return mounthBegin;
	}
	
	/**
	 * return report HTML page with data by default. 
	 * Default - Purchase Transactions ordered by category from first day of month to now
	 * @param model
	 * @param locale
	 * @return report HTML page with data by default
	 */
	@GetMapping("/reports")
	public String getReport(Model model, Locale locale) {
		
		model.addAttribute("chartData",getPurchaseTransactionOrderedByCategory(getFirstDayMonth(), new Date()));
		
		model.addAttribute("reportTitle", messageSource.getMessage("reports.pie.categories", null, locale));
		
		model.addAttribute("reportModel", new ReportModel());
		
		return "report-pie";
	}
}
