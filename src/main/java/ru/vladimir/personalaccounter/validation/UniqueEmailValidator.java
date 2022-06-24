/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.vladimir.personalaccounter.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.service.UserService;

/**
 *
 * @author vladimir
 */
class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String>{
    @Autowired
    private UserService userService;

    private String message;

    @Override
    public void initialize(UniqueEmail constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        AppUser userIndb = userService.findByEmail(email);
        if (userIndb == null) {
            return true;
        } else {
            context.buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }
    }
    
}
