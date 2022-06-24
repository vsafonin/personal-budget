package ru.vladimir.personalaccounter.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.vladimir.personalaccounter.enums.TypeOfOperation;

/**
 *
 * @author vladimir
 */
@Entity
@Table(name = "purchase_transaction")
@Getter
@Setter
@AllArgsConstructor
public class PurchaseTransaction extends AbstractTransaction implements Serializable {
    
    private static final long serialVersionUID = 1L;
	    
    @ManyToOne
    @JoinColumn(name = "shop_id")
    @Valid
    private Shop shop; 
    
    @ManyToMany(cascade = CascadeType.ALL)
    @Valid
    List<ProductData> productDatas;
    
    @Column(name = "fiscal_sign", nullable = true)
    private long fiscalSign;
    
    @Column(name = "from_jsnon")
    private boolean fromJson;
    
    public String getProductNameString() {
    	StringBuilder result = new StringBuilder();
    	productDatas.stream().forEach(t -> result.append(t.getProduct().getName()).append(","));
    	return result.toString();
    }
    
    public PurchaseTransaction() {
    	//purchase transaction always is decrease
    	this.setTypeOfOperation(TypeOfOperation.DECREASE);
    }
}
