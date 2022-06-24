package ru.vladimir.personalaccounter.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
@Component
public class MyAythentificationProvider implements AuthenticationProvider {
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String name = authentication.getName();
		if(name.isEmpty()) {
			throw new BadCredentialsException("mising username");
		}
		
		UserDetails user = null;
		try {
			user = userDetailsService.loadUserByUsername(name);
		}
		catch (UsernameNotFoundException exp) {
			throw new BadCredentialsException("invalid login details");
		}
		return createSuccessfulAuthentication(authentication, user);
	}

	private Authentication createSuccessfulAuthentication(Authentication authentication, UserDetails user) {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getUsername(), authentication.getCredentials(), user.getAuthorities());
        token.setDetails(authentication.getDetails());
        return token;
	}

	@Override
	public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}
