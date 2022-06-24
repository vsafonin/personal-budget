package ru.vladimir.personalaccounter.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *this class user for store product with cost and count
 * we can't store cost and count in product class cause this is changeable data
 * @author vladimir
 */
@Entity
@Table(name = "product_data")
@Getter
@Setter
@NoArgsConstructor
public class ProductData implements Serializable {
 
    private static final long serialVersionUID = -5253556647658900832L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    
    @Column(name = "cost")
    @DecimalMin(value = "0.01", message = "{productData.validation.message.cost}")
    @NotNull
    private BigDecimal cost;
    
    @Column(name = "quantity")
    @DecimalMin(value = "0.01", message = "{productData.validation.message.quantity}")
    @NotNull
    private BigDecimal quantity;
    
    @ManyToOne
    @JoinColumn(name = "product_id")
    @Valid
    private Product product;
}
