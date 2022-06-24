package ru.vladimir.personalaccounter.security;

import static org.springframework.util.StringUtils.hasText;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.methods.JwtProvider;
import ru.vladimir.personalaccounter.service.UserService;


@Component
public class JwtFilter extends GenericFilterBean {

	public static final String AUTHORIZATION = "Authorization";

	
	
	@Autowired
	private JwtProvider jwtProvider;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private UserService userService;
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String jwtToken = getTokenFromRequest((HttpServletRequest) request);
		if (jwtToken != null && jwtProvider.validateToken(jwtToken)) {
			String userName = jwtProvider.getLoginFromToken(jwtToken);
			UserDetails theAppUser = userDetailsService.loadUserByUsername(userName);
			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(theAppUser, null,
					theAppUser.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(auth);
			AppUser theUser = (AppUser) auth.getPrincipal();
			userService.setLastLogin(theUser);
		}
		chain.doFilter(request, response);
	}
	
	private String getTokenFromRequest(HttpServletRequest httpServletRequest) {
		String bearer = httpServletRequest.getHeader(AUTHORIZATION);
		if (hasText(bearer) && bearer.startsWith("Bearer ")) {
			return bearer.substring(7);
		}
		return null;
	}

}
