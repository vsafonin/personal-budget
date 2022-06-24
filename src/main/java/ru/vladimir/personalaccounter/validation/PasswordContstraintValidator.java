/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.vladimir.personalaccounter.validation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.passay.WhitespaceRule;
import org.passay.spring.SpringMessageResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 *
 * @author vladimir
 */
@Component
class PasswordContstraintValidator implements ConstraintValidator<ValidPassword, String> {
    
    @Autowired
    @Qualifier("passayBundleMessageSource")
    private MessageSource messageSource;
    

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
    	//check for null password
    	if (password == null) {
    		return false;
    	}
        SpringMessageResolver springMessageResolver = new SpringMessageResolver(messageSource);
        PasswordValidator validator = new PasswordValidator(springMessageResolver,Arrays.asList(
                //at least 8 characters max 30
                new LengthRule(8, 30),
                //at least one uppercase character
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                //at least one lowercase character
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                //at least one digt character
                new CharacterRule(EnglishCharacterData.Digit, 1),
                //prohibit spaces
                new WhitespaceRule()
        ));

        RuleResult result = validator.validate(new PasswordData(password));
        if (result.isValid()) {
            return true;
        }
        
        List<String> messages = validator.getMessages(result);
        String messageTemplate = messages.stream()
                .collect(Collectors.joining(","));
        context.buildConstraintViolationWithTemplate(messageTemplate)
                .addConstraintViolation()
                .disableDefaultConstraintViolation();
        return false;

    }

}
