package ru.vladimir.personalaccounter.config;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
/**
 * Main configuration for spring boot app.
 * 
 * @author Vladimir Afonin
 *
 */
@Configuration
public class SpringConfig implements WebMvcConfigurer{

	
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor()).addPathPatterns("/**");
    }

    
    
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.US);
    	return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Bean
    public ResourceBundleMessageSource passayBundleMessageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages/passay/messages");
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }
    
    @Bean
    @Primary
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages/messages");
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }
    
    @Bean
    @Primary
    public SpringTemplateEngine springTemplateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(htmlTemplateResolver());
        templateEngine.setTemplateEngineMessageSource(messageSource());
        return templateEngine;
    }
    
    @Bean
    public SpringResourceTemplateResolver htmlTemplateResolver(){
        SpringResourceTemplateResolver thymeleafTemplateResolver = new SpringResourceTemplateResolver();
        thymeleafTemplateResolver.setPrefix("classpath:/templates/");
        thymeleafTemplateResolver.setSuffix(".html");
        thymeleafTemplateResolver.setTemplateMode(TemplateMode.HTML);
        thymeleafTemplateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        
        return thymeleafTemplateResolver;
    }
    
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        int stranght = 10;
        return new BCryptPasswordEncoder(stranght, new SecureRandom());
    }

}
