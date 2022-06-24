package ru.vladimir.personalaccounter.rest;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.Authorities;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.entity.DebtTransaction;
import ru.vladimir.personalaccounter.entity.Partner;
import ru.vladimir.personalaccounter.enums.AauthoritiesEnum;
import ru.vladimir.personalaccounter.enums.TypeOfOperation;
import ru.vladimir.personalaccounter.methods.JwtProvider;
import ru.vladimir.personalaccounter.repository.AppUserJwtRepository;
import ru.vladimir.personalaccounter.repository.PartnerRepository;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.DebtTransactionService;
import ru.vladimir.personalaccounter.service.UserService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = DebTransactionRestController.class)
class DebTransactionRestControllerTest {

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
    private DebtTransactionService debtTransactionService;

    @MockBean
    private BankAccountService bankAccountService;
    
    @MockBean
    private PartnerRepository partnerRepository;

    @Mock
    private Authentication auth;

    @Autowired
    private ObjectMapper mapper;

    private AppUser theAppUser;
    
    @MockBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        theAppUser = new AppUser("test", "test", "123", "123", "pupa@mail.ru", true);
        theAppUser.setRoles(Set.of(new Authorities(AauthoritiesEnum.USER.getRoleName())));
        when(auth.getPrincipal()).thenReturn(theAppUser);
        SecurityContextHolder.getContext().setAuthentication(auth);
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theAppUser);

    }

    @Test
    void testGetDebtShouldBeOkIncrease() throws Exception {
        when(jwtProvider.validateToken(TOKEN)).thenReturn(true);
        when(jwtProvider.getLoginFromToken(TOKEN)).thenReturn("test");
        when(userDetailsService.loadUserByUsername("test")).thenReturn(theAppUser);
        when(debtTransactionService.findAll()).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/api/debt").header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .param("type", "INCREASE")
        ).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetDebtShouldBeOkDecrease() throws Exception {
        when(jwtProvider.validateToken(TOKEN)).thenReturn(true);
        when(jwtProvider.getLoginFromToken(TOKEN)).thenReturn("test");
        when(userDetailsService.loadUserByUsername("test")).thenReturn(theAppUser);
        when(debtTransactionService.findAll()).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/api/debt").header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .param("type", "DECREASE")
        ).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    //post
    @Test
    void testPostDebtShouldBeFailBankAccountNameIsEmpty() throws Exception {
        when(jwtProvider.validateToken(TOKEN)).thenReturn(true);
        when(jwtProvider.getLoginFromToken(TOKEN)).thenReturn("test");
        when(userDetailsService.loadUserByUsername("test")).thenReturn(theAppUser);

        BankAccount theBankAccount = new BankAccount();
        theBankAccount.setName("");
        DebtTransaction thedebtTransaction = new DebtTransaction();
        thedebtTransaction.setBankAccount(theBankAccount);
        thedebtTransaction.setTypeOfOperation(TypeOfOperation.DECREASE);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/debt").header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .param("type", "DECREASE").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(thedebtTransaction))
        )
                .andExpect(status().isUnprocessableEntity()) 
                .andExpect(content().string("Bank Account name is null or empty"));
    }
    @Test
    void testPostDebtShouldBeFailBankAccountNotFound() throws Exception {
        when(jwtProvider.validateToken(TOKEN)).thenReturn(true);
        when(jwtProvider.getLoginFromToken(TOKEN)).thenReturn("test");
        when(userDetailsService.loadUserByUsername("test")).thenReturn(theAppUser);
        

        BankAccount theBankAccount = new BankAccount();
        theBankAccount.setName("sad");
        when(bankAccountService.getBankAccounts()).thenReturn(new ArrayList<>());
        
        DebtTransaction thedebtTransaction = new DebtTransaction();
        thedebtTransaction.setBankAccount(theBankAccount);
        thedebtTransaction.setTypeOfOperation(TypeOfOperation.DECREASE);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/debt").header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .param("type", "DECREASE").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(thedebtTransaction))
        )
                .andExpect(status().isUnprocessableEntity()) 
                .andExpect(content().string("BankAccount not found"));
    }
    @Test
    void testPostDebtShouldBeFailEndDateIsNull() throws Exception {
        when(jwtProvider.validateToken(TOKEN)).thenReturn(true);
        when(jwtProvider.getLoginFromToken(TOKEN)).thenReturn("test");
        when(userDetailsService.loadUserByUsername("test")).thenReturn(theAppUser);
        

        BankAccount theBankAccount = new BankAccount();
        theBankAccount.setName("sad");
        theBankAccount.setCurrency(Currency.getInstance(Locale.ITALY));
        when(bankAccountService.getBankAccounts()).thenReturn(List.of(theBankAccount));
        
        DebtTransaction thedebtTransaction = new DebtTransaction();
        thedebtTransaction.setBankAccount(theBankAccount);
        thedebtTransaction.setTypeOfOperation(TypeOfOperation.DECREASE);
        

        mockMvc.perform(MockMvcRequestBuilders.post("/api/debt").header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(thedebtTransaction))
        )
                .andExpect(status().isUnprocessableEntity()) 
                .andExpect(content().string("End date is null"));
    }
    @Test
    void testPostDebtShouldBeFailSum0() throws Exception {
        when(jwtProvider.validateToken(TOKEN)).thenReturn(true);
        when(jwtProvider.getLoginFromToken(TOKEN)).thenReturn("test");
        when(userDetailsService.loadUserByUsername("test")).thenReturn(theAppUser);
        

        BankAccount theBankAccount = new BankAccount();
        theBankAccount.setName("sad");
        theBankAccount.setCurrency(Currency.getInstance(Locale.ITALY));
        when(bankAccountService.getBankAccounts()).thenReturn(List.of(theBankAccount));
        
        DebtTransaction thedebtTransaction = new DebtTransaction();
        thedebtTransaction.setBankAccount(theBankAccount);
        thedebtTransaction.setEndDate(new Timestamp(System.currentTimeMillis() + (5 *24 * 60 * 60 * 1000))); //+5 days
        thedebtTransaction.setTypeOfOperation(TypeOfOperation.DECREASE);
        thedebtTransaction.setSumTransaction(BigDecimal.ZERO);
        

        mockMvc.perform(MockMvcRequestBuilders.post("/api/debt").header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(thedebtTransaction))
        )
                .andExpect(status().isUnprocessableEntity()) 
                .andExpect(content().string("Sum is 0 or less"));
    }
    @Test
    void testPostDebtShouldBeFailPartnerIsNull() throws Exception {
        when(jwtProvider.validateToken(TOKEN)).thenReturn(true);
        when(jwtProvider.getLoginFromToken(TOKEN)).thenReturn("test");
        when(userDetailsService.loadUserByUsername("test")).thenReturn(theAppUser);
        

        BankAccount theBankAccount = new BankAccount();
        theBankAccount.setName("sad");
        theBankAccount.setCurrency(Currency.getInstance(Locale.ITALY));
        when(bankAccountService.getBankAccounts()).thenReturn(List.of(theBankAccount));
        
        DebtTransaction thedebtTransaction = new DebtTransaction();
        thedebtTransaction.setBankAccount(theBankAccount);
        thedebtTransaction.setEndDate(new Timestamp(System.currentTimeMillis() + (5 *24 * 60 * 60 * 1000))); //+5 days
        thedebtTransaction.setSumTransaction(BigDecimal.TEN);
        thedebtTransaction.setTypeOfOperation(TypeOfOperation.DECREASE);
        

        mockMvc.perform(MockMvcRequestBuilders.post("/api/debt").header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .param("type", "DECREASE").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(thedebtTransaction))
        )
                .andExpect(status().isUnprocessableEntity()) 
                .andExpect(content().string("Partner is null"));
    }
    @Test
    void testPostDebtShouldBeFailPartnerNameIsEmpty() throws Exception {
        when(jwtProvider.validateToken(TOKEN)).thenReturn(true);
        when(jwtProvider.getLoginFromToken(TOKEN)).thenReturn("test");
        when(userDetailsService.loadUserByUsername("test")).thenReturn(theAppUser);
        

        BankAccount theBankAccount = new BankAccount();
        theBankAccount.setName("sad");
        theBankAccount.setCurrency(Currency.getInstance(Locale.ITALY));
        when(bankAccountService.getBankAccounts()).thenReturn(List.of(theBankAccount));
        
        DebtTransaction thedebtTransaction = new DebtTransaction();
        thedebtTransaction.setBankAccount(theBankAccount);
        thedebtTransaction.setEndDate(new Timestamp(System.currentTimeMillis() + (5 *24 * 60 * 60 * 1000))); //+5 days
        thedebtTransaction.setSumTransaction(BigDecimal.TEN);
        Partner thePartner = new Partner();
        thePartner.setName("");
        thedebtTransaction.setPartner(thePartner);
        thedebtTransaction.setTypeOfOperation(TypeOfOperation.DECREASE);
        
        

        mockMvc.perform(MockMvcRequestBuilders.post("/api/debt").header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .param("type", "DECREASE").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(thedebtTransaction))
        )
                .andExpect(status().isUnprocessableEntity()) 
                .andExpect(content().string("Partner name is null or empty"));
    }
    
    @Test
    void testPostDebtShouldBeOkPartnerNameIsNotFound() throws Exception {
        when(jwtProvider.validateToken(TOKEN)).thenReturn(true);
        when(jwtProvider.getLoginFromToken(TOKEN)).thenReturn("test");
        when(userDetailsService.loadUserByUsername("test")).thenReturn(theAppUser);
        

        BankAccount theBankAccount = new BankAccount();
        theBankAccount.setName("sad");
        theBankAccount.setCurrency(Currency.getInstance(Locale.ITALY));
        when(bankAccountService.getBankAccounts()).thenReturn(List.of(theBankAccount));
        
        DebtTransaction thedebtTransaction = new DebtTransaction();
        thedebtTransaction.setBankAccount(theBankAccount);
        thedebtTransaction.setEndDate(new Timestamp(System.currentTimeMillis() + (5 *24 * 60 * 60 * 1000))); //+5 days
        thedebtTransaction.setSumTransaction(BigDecimal.TEN);
        Partner thePartner = new Partner();
        thePartner.setName("sad");
        when(partnerRepository.findByName(theAppUser, "sad")).thenReturn(null);
        when(partnerRepository.save(thePartner)).thenReturn(thePartner);
        thedebtTransaction.setPartner(thePartner);
        thedebtTransaction.setTypeOfOperation(TypeOfOperation.DECREASE);
        

        mockMvc.perform(MockMvcRequestBuilders.post("/api/debt").header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(thedebtTransaction))
        )
                .andExpect(status().isOk()) ;
        Mockito.verify(partnerRepository,Mockito.times(1)).save(thePartner);
    }
    @Test
    void testPostDebtShouldBeOkDecrease() throws Exception {
        when(jwtProvider.validateToken(TOKEN)).thenReturn(true);
        when(jwtProvider.getLoginFromToken(TOKEN)).thenReturn("test");
        when(userDetailsService.loadUserByUsername("test")).thenReturn(theAppUser);
        

        BankAccount theBankAccount = new BankAccount();
        theBankAccount.setName("sad");
        theBankAccount.setCurrency(Currency.getInstance(Locale.ITALY));
        when(bankAccountService.getBankAccounts()).thenReturn(List.of(theBankAccount));
        
        DebtTransaction thedebtTransaction = new DebtTransaction();
        thedebtTransaction.setBankAccount(theBankAccount);
        thedebtTransaction.setEndDate(new Timestamp(System.currentTimeMillis() + (5 *24 * 60 * 60 * 1000))); //+5 days
        thedebtTransaction.setSumTransaction(BigDecimal.TEN);
        Partner thePartner = new Partner();
        thePartner.setName("sad");
        when(partnerRepository.findByName(theAppUser, "sad")).thenReturn(thePartner);
        thedebtTransaction.setPartner(thePartner);
        thedebtTransaction.setTypeOfOperation(TypeOfOperation.DECREASE);
        when(debtTransactionService.save(ArgumentMatchers.any(DebtTransaction.class))).thenReturn(thedebtTransaction);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/debt").header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(thedebtTransaction))
        )
                .andExpect(status().isOk()) 
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        Mockito.verify(debtTransactionService,Mockito.times(1)).save(ArgumentMatchers.any(DebtTransaction.class));
    }
    @Test
    void testPostDebtShouldBeOkIncrease() throws Exception {
        when(jwtProvider.validateToken(TOKEN)).thenReturn(true);
        when(jwtProvider.getLoginFromToken(TOKEN)).thenReturn("test");
        when(userDetailsService.loadUserByUsername("test")).thenReturn(theAppUser);
        

        BankAccount theBankAccount = new BankAccount();
        theBankAccount.setName("sad");
        theBankAccount.setCurrency(Currency.getInstance(Locale.ITALY));
        when(bankAccountService.getBankAccounts()).thenReturn(List.of(theBankAccount));
        
        DebtTransaction thedebtTransaction = new DebtTransaction();
        thedebtTransaction.setBankAccount(theBankAccount);
        thedebtTransaction.setEndDate(new Timestamp(System.currentTimeMillis() + (5 *24 * 60 * 60 * 1000))); //+5 days
        thedebtTransaction.setSumTransaction(BigDecimal.TEN);
        Partner thePartner = new Partner();
        thePartner.setName("sad");
        when(partnerRepository.findByName(theAppUser, "sad")).thenReturn(thePartner);
        thedebtTransaction.setPartner(thePartner);
        thedebtTransaction.setTypeOfOperation(TypeOfOperation.INCREASE);
        when(debtTransactionService.save(ArgumentMatchers.any(DebtTransaction.class))).thenReturn(thedebtTransaction);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/debt").header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(thedebtTransaction))
        )
                .andExpect(status().isOk()) 
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        Mockito.verify(debtTransactionService,Mockito.times(1)).save(ArgumentMatchers.any(DebtTransaction.class));
    }

}
