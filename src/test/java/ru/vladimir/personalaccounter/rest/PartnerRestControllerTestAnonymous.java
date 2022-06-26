package ru.vladimir.personalaccounter.rest;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.Partner;
import ru.vladimir.personalaccounter.methods.JwtProvider;
import ru.vladimir.personalaccounter.repository.PartnerRepository;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.UserService;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(PartnerRestContoller.class)
@WithMockUser
class PartnerRestControllerTestAnonymous {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private UserDetailsService userDetailsService;
	
	@MockBean
	private BankAccountService bankAccountService;
	
	@MockBean
	private JwtProvider jwtProvider;
	
	@MockBean
	private UserService userService;

	
	@MockBean
	private PartnerRepository partnerRepository;

	@Test
	void testGetProductFromDb_should_be_ok() throws Exception {
		AppUser theAppUser = new AppUser();
		Partner thePartner = new Partner();
		thePartner.setName("test");
		thePartner.setAppUser(theAppUser);
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theAppUser);
		when(partnerRepository.findAllByAppUser(theAppUser)).thenReturn(List.of(thePartner));
		mockMvc.perform(get("/api/partner/?q=tes").contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0]",is(thePartner.getName())));
	
	}

}
