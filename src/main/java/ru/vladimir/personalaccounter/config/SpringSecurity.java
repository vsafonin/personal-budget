package ru.vladimir.personalaccounter.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.security.CustomAccessDeniedHandler;
import ru.vladimir.personalaccounter.security.JwtFilter;
import ru.vladimir.personalaccounter.service.UserService;
/**
 * Configurations class for security settings.
 * Contains 2 internal classes for separate security settings HTML on web forms and rest api methods.
 * 
 * @author Vladimir Afonin
 *
 */
/*
 * i separate this cause i want use CSRF protection for HTML on web forms and not in api methods 
 * 
 */
@Configuration
@EnableWebSecurity
public class SpringSecurity extends WebSecurityConfigurerAdapter {
	
	private final String COOCKIE_ENCRYPT_KEY="TOWpVAuUXb";
	private final String COOCKIE_NAME="personal-budget-remmember-me";

	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private UserService userService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	/**
	 * inner class for rest api security settings
	 * @author Vladimir Afonin
	 *
	 */
	@Configuration
	@Order(1)
	public class ApiWebSecurityConfig extends WebSecurityConfigurerAdapter {

		@Autowired
		private JwtFilter jwtFilter;

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.csrf().disable()
				.antMatcher("/api/**")
				.authorizeRequests()
				.antMatchers("/api/**").hasRole("user")
				.and()
				.httpBasic().and().addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
			
			http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		}

	}

	/**
	 * inner class for HTML security settings
	 * @author Vladimir Afonin
	 *
	 */
	@Configuration
	@Order(2)
	public class FormWebSecurity extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.authorizeRequests().antMatchers("/", "/css/**", "/js/**", "/registration/**", "/recover/**").permitAll()
					.antMatchers("/user/**").hasRole("user").anyRequest().authenticated()
					.and().formLogin().loginPage("/login").permitAll()
					.successHandler(handleThatUserSuccesAuthAndStoreInDb())
					.and().exceptionHandling()
					.accessDeniedHandler(accessDeniedHandler()).and().logout().invalidateHttpSession(true)
					.clearAuthentication(true).permitAll().deleteCookies(COOCKIE_NAME).permitAll()
					.and().rememberMe() // TODO исправить, не работает
					.key(COOCKIE_ENCRYPT_KEY).rememberMeCookieName(COOCKIE_NAME)
					.tokenValiditySeconds(2592000); // 30days
		}

		@Bean
		public AccessDeniedHandler accessDeniedHandler() {
			return new CustomAccessDeniedHandler();
		}

	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(daoAuthenticationProvider());
	}


	@Bean
	public DaoAuthenticationProvider daoAuthenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(userDetailsService);
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
		return daoAuthenticationProvider;
	}

	/**
	 * handle when user is successful logged in form login (/login) 
	 * and store in field lastLogin in appUser entity
	 * and store in DB. 
	 * @return
	 */
	private  AuthenticationSuccessHandler handleThatUserSuccesAuthAndStoreInDb() {
		return new AuthenticationSuccessHandler() {
			
			@Override
			public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
					Authentication authentication) throws IOException, ServletException {
				//store last login information in db
				AppUser theUser = (AppUser) authentication.getPrincipal();
				userService.setLastLogin(theUser);
				response.sendRedirect(request.getContextPath());
			}
		};
	}
	
}
