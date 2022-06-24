package ru.vladimir.personalaccounter.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_user_recover_password")
@Getter
@Setter
@NoArgsConstructor
public class AppUserRecoveryPasswordModel {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;
	
	@ManyToOne
	private AppUser appUse;
	
	@Column(name = "token")
	private String token;
	
	@Column(name = "expired_date")
	private Date expiredDate;
}
