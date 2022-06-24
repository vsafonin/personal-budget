/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package ru.vladimir.personalaccounter.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.entity.Category;
import ru.vladimir.personalaccounter.entity.Product;
import ru.vladimir.personalaccounter.entity.ProductData;
import ru.vladimir.personalaccounter.entity.PurchaseTransaction;
import ru.vladimir.personalaccounter.entity.Shop;
import ru.vladimir.personalaccounter.enums.TypeOfOperation;

/**
 *
 * @author vladimir
 */
@SpringBootTest
public class PurchaseTransactionInterfaceTest {
    
    @Autowired
    private PurchaseTransactionRepository repository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ShopRepository shopRepository;
    
    @Autowired
    private BankAccountRepository bankAccountRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public PurchaseTransactionInterfaceTest() {
    }

    @Test
    public void testSaveShouldBeOk() throws InterruptedException {
        //we need to create user
        AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
        userRepository.save(theTestUser);
        //we need to create bank account
        BankAccount theTestBankAccount = new BankAccount("test", BigDecimal.TEN, "test bank", theTestUser, Currency.getInstance("RUB"));
        bankAccountRepository.save(theTestBankAccount);
        //we need to create category
        Category category = new Category();
        category.setName("продукты");
        categoryRepository.save(category);
        Category category1 = new Category();
        category.setName("хозтовары");
        categoryRepository.save(category1);
        //we need to create product
        Product product = new Product();
        product.setName("test");        
        product.setCategory(category);
        productRepository.save(product);
        
        Product product2 = new Product();
        product2.setName("test2");        
        product2.setCategory(category1);
        productRepository.save(product2);
       
        //we need to create shop
        Shop shop = new Shop();
        shop.setName("5ka");
        shopRepository.save(shop);
        
        //we need create purchase transaction 
        PurchaseTransaction purchaseTransaction = new PurchaseTransaction();
        purchaseTransaction.setAppUser(theTestUser);
        purchaseTransaction.setBankAccount(theTestBankAccount);
        purchaseTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
        purchaseTransaction.setCurrency(Currency.getInstance("RUB"));
        purchaseTransaction.setShop(shop);
        purchaseTransaction.setTypeOfOperation(TypeOfOperation.DECREASE);
        ProductData productData = new ProductData();
        productData.setCost(BigDecimal.valueOf(100));
        productData.setQuantity(BigDecimal.TEN);
        productData.setProduct(product);
        ProductData productData1 = new ProductData();
        productData1.setCost(BigDecimal.valueOf(200));
        productData1.setQuantity(BigDecimal.ONE);
        productData1.setProduct(product2);
        List<ProductData> productDatas = new ArrayList<>();
        productDatas.add(productData);
        productDatas.add(productData1);
        purchaseTransaction.setProductDatas(productDatas);
        
        //save it
        Long id = repository.save(purchaseTransaction).getId();
        
        //retrieve from DB
        Optional<PurchaseTransaction> optionalShopTransactionFromDb = repository.findById(id);
        assertThat(optionalShopTransactionFromDb.isPresent()).isTrue();
        
        //print transaction
        PurchaseTransaction shopTransactionFromDb = optionalShopTransactionFromDb.get();
        assertThat(shopTransactionFromDb.getProductDatas().contains(productData));
        assertThat(shopTransactionFromDb.getProductDatas().contains(productData1));
        for (ProductData productDataItem: shopTransactionFromDb.getProductDatas()) {
            System.out.println(productDataItem.getProduct().getName());
        }
    }
    
}
