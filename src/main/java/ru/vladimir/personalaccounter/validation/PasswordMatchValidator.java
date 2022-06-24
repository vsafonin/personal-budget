/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.vladimir.personalaccounter.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.BeanWrapperImpl;

/**
 *
 * @author vladimir
 */
class PasswordMatchValidator implements ConstraintValidator<FieldMatch, Object>{
    
    private String password;
    private String passwordConfirm;
    private String message;

    @Override
    public void initialize(FieldMatch constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
        this.password = constraintAnnotation.first();
        this.passwordConfirm = constraintAnnotation.second();
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        boolean valid = true;
        try {
           final Object  firstObj = new BeanWrapperImpl(value).getPropertyValue(password);
           final Object  secondObj = new BeanWrapperImpl(value).getPropertyValue(passwordConfirm);
           valid = firstObj == null && secondObj == null || firstObj != null && firstObj.equals(secondObj);
        }
        catch (final Exception ignore) {
            //ignore exception
        }
        if (!valid) {
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(password)
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
                    
                    
        }
        
        return valid;
    }
    
}
