package ru.vladimir.personalaccounter.entity;

import java.io.Serializable;
import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "activation_links")
@NoArgsConstructor
@Getter
@Setter
public class ActivationUuid implements Serializable {
    
   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id")
   private long id;
   
   @Column(name = "expired_date")
   private ZonedDateTime expiredDate;
   
   @Column(name = "uuid")
   private String uuid;
   
   @OneToOne(mappedBy = "activationLink")
   private AppUser appUser;	
   
}
