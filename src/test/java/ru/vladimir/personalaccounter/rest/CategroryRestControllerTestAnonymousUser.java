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
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import ru.vladimir.personalaccounter.entity.Category;
import ru.vladimir.personalaccounter.methods.JwtProvider;
import ru.vladimir.personalaccounter.repository.CategoryRepository;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.UserService;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(CategroryRestController.class)
@WithAnonymousUser
class CategroryRestControllerTestAnonymousUser {
	
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
	private CategoryRepository categoryRepository;

	@Test
	void testGetCategoryFromDb_should_be_ok() throws Exception {
		Category theCategory = new Category();
		theCategory.setName("test");
		when(categoryRepository.findAll()).thenReturn(List.of(theCategory));
		
		mockMvc.perform(get("/api/category/?q=tes").contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0]",is(theCategory.getName())));
	
	}

}
