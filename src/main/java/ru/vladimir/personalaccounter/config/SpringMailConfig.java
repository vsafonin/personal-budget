package ru.vladimir.personalaccounter.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
/**
 * Class configuration for mail sender. 
 * Sets needed parameter for work with mail server.
 * Sets another HTML template and message resolver.
 * message.properties and template you can see in mail/* directory
 * @author Vladimir Afonin
 *
 */
@Configuration
@PropertySource("classpath:mail/emailconfig.properties")
public class SpringMailConfig implements EnvironmentAware{

    private static final String MAIL_HOST = "mail.server.host";
    private static final String MAIL_PORT = "mail.server.port";
    private static final String MAIL_USERNAME = "mail.server.username";
    private static final String MAIL_PASSWORD = "mail.server.password";

    private Environment environment;
    
    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }
    
    @Bean
    public JavaMailSender mailSender() throws IOException {

		JavaMailSenderImpl javaMailSenderImpl = new JavaMailSenderImpl();
		javaMailSenderImpl.setHost(environment.getProperty(MAIL_HOST));
		javaMailSenderImpl.setPort(convertStringToInt(environment.getProperty(MAIL_PORT)));
		javaMailSenderImpl.setUsername(environment.getProperty(MAIL_USERNAME));
		javaMailSenderImpl.setPassword(environment.getProperty(MAIL_PASSWORD));
		

		// set SMTP parameters
		Properties props = javaMailSenderImpl.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.ssl.enable", "true");
		props.put("mail.debug", "true");

		return javaMailSenderImpl;

    }

    private int convertStringToInt(String property) {
    	try {
    		Integer result = Integer.parseInt(property); 
    		return result;
    	}
    	catch (NumberFormatException e) {
    		throw new RuntimeException("Error format property file");
    	}
	}

	@Bean
    @Qualifier("emailMessageSource")
    public ResourceBundleMessageSource emailMessageSource() {
        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages/mail/messages");
        return messageSource;
    }
	
    /*
     * Create bean for email template engine
     * with this we can use thymeleaf for email messages  
     *  
     */
    @Bean
    @Qualifier("emailTemplateEngine")
    public SpringTemplateEngine emailTemplateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(htmlEmailTemplateResolver());
        templateEngine.setTemplateEngineMessageSource(emailMessageSource());
        return templateEngine;
    }
    @Bean
    public SpringResourceTemplateResolver htmlEmailTemplateResolver(){
        SpringResourceTemplateResolver emailTemplateResolver = new SpringResourceTemplateResolver();
        emailTemplateResolver.setPrefix("classpath:/templates/mail/");
        emailTemplateResolver.setSuffix(".html");
        emailTemplateResolver.setTemplateMode(TemplateMode.HTML);
        emailTemplateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        
    return emailTemplateResolver;
    }
	
   
    
}
