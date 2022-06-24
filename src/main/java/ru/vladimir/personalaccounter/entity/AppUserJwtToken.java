package ru.vladimir.personalaccounter.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "app_user_jwt_token")
public class AppUserJwtToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;
	
	@Column(name = "token")
	private String token;
	
	@Column(name = "expired_date")
	private Date expDate;
	
	@ManyToOne
	@JoinColumn(name = "app_user_id", nullable = false)
	private AppUser appUser;
	
}
