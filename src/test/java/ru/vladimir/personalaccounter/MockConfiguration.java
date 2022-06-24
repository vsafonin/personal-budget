package ru.vladimir.personalaccounter;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;

import ru.vladimir.personalaccounter.methods.JwtProvider;
import ru.vladimir.personalaccounter.repository.AppUserJwtRepository;
import ru.vladimir.personalaccounter.service.UserService;

@TestConfiguration	
public class MockConfiguration {

	@Bean
	public JwtProvider getJwtProvider() {
		JwtProvider jwtProvider = mock(JwtProvider.class);
		return jwtProvider;
	}
	
	
	@Bean
	public UserDetailsService getUserDetailsService() {
		UserDetailsService detailsService = mock(UserDetailsService.class);
		return detailsService;
				
	}
	
	@Bean
	public AppUserJwtRepository getAppUserJwtRepository() {
		AppUserJwtRepository appUserJwtRepository = mock(AppUserJwtRepository.class);
		return appUserJwtRepository;
	}
	
	@Bean
	public UserService getUserService() {
		UserService userService = mock(UserService.class);
		return userService;
	}
	
	
}
