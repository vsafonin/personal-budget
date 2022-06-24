package ru.vladimir.personalaccounter.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
			
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if( authentication != null) {
			log.info("User '" + authentication.getName() + 
					"' attemped to acces the URL: " +
					request.getRequestURI());
		}
		
		response.sendRedirect(request.getContextPath() + "/access-denied");
	}

}
