package ru.vladimir.personalaccounter.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author vladimir
 */
@Entity
@Table(name = "category")
@Getter
@Setter
@NoArgsConstructor
public class Category implements Serializable {
    
    public Category(String categoryName) {
    	this.name = categoryName;
    }

	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    
    @Column(name = "name")
    @NotEmpty(message = "{category.validation.message.name}")
    private String name;
    
}
