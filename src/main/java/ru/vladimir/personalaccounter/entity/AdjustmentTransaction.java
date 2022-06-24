package ru.vladimir.personalaccounter.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/**
 * this class i use for save history of transaction
 *we has 2 type of operations:
 *DECREASE:
 *	1) bying in shop
 *	2) Adjustment which decrease current account
 *INCREASE:
 *	1) Salary
 *	2) Adjustment which increase current account
 * @author vladimir
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "adjustment_transactions")
public class AdjustmentTransaction extends AbstractTransaction implements Cloneable, Serializable{
	
    private static final long serialVersionUID = 1L;
    

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
        
        

}
