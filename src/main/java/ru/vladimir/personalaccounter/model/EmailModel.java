package ru.vladimir.personalaccounter.model;

import java.util.Locale;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class EmailModel {
    private String emailTo;
    private Map<String, Object> parametrs;
    private Locale locale;
    
}
