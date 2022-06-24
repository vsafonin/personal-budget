/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package ru.vladimir.personalaccounter.enums;

/**
 *
 * @author vladimir
 */
public enum AauthoritiesEnum {
    DEMO("demo"),
    USER("user"),
    ADMIN("admin");
    
    private String message;

    private AauthoritiesEnum(String message) {
        this.message = "ROLE_" + message;
    }

    public String getRoleName() {
        return message;
    }
    
    
    
}
