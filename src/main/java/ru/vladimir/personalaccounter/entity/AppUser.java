package ru.vladimir.personalaccounter.entity;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import ru.vladimir.personalaccounter.validation.FieldMatch;
import ru.vladimir.personalaccounter.validation.UniqueEmail;
import ru.vladimir.personalaccounter.validation.UniqueUsername;
import ru.vladimir.personalaccounter.validation.ValidPassword;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@FieldMatch.List(@FieldMatch(first = "password", second = "passwordConfirm", message = "#{}"))
public class AppUser implements Serializable, UserDetails {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;

	@Column(name = "username", unique = true)
	@NotBlank
	@Length(min = 2, max = 256, message = "{user.vlidation.message.username}")
	@UniqueUsername(message = "{user.vlidation.message.usernameUnique}")
	private String username;

	@Column(name = "displayName")
	@NotBlank
	@Length(min = 2, max = 256, message = "{user.vlidation.message.displayname}")
	private String displayName;

	@Column(name = "password")
	@NotBlank
	@ValidPassword
	private String password;

	@Transient
	private String passwordConfirm;
	
	@Transient
	private String passwordInputField;
	
	@Column(name = "email", unique = true)
	@Email(message = "#{user.vlidation.message.email}")
	@UniqueEmail(message = "#{user.vlidation.message.emailUnique}")
	private String email;

	@Column(name = "create_time")
	private ZonedDateTime createTime;

	@Column(name = "enabled")
	private boolean enabled = false;

	@Column(name = "last_login")
	private ZonedDateTime lastLogin;

	@ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH })
	@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "users_id"), inverseJoinColumns = @JoinColumn(name = "roles_id"))
	private Set<Authorities> roles;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "activation_link_id", referencedColumnName = "id")
	private ActivationUuid activationLink;
	
	@OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL)
	@JsonIgnore
	private Set<BankAccount> bankAccounts;
	
	@Getter
	@Setter
	@OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<AppUserJwtToken> appUserJwtTokens;
	
	public AppUser() {
		if (this.createTime == null) {
			this.createTime = ZonedDateTime.now();
		}
	}
	
	// additional constructor for convenience testing
	public AppUser(
			String username,
			String displayName,
			String password,
			String passwordConfirm,
			String email,
			boolean enabled) {
		
		super();
		this.username = username;
		this.displayName = displayName;
		this.password = password;
		this.passwordConfirm = passwordConfirm;
		this.email = email;
		this.enabled = enabled;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return roles;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return enabled;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, username);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AppUser other = (AppUser) obj;
		if (other.getId() == 0 && this.id  != 0 ||
				other.getId() != 0 && this.id == 0) {
			return false;
		}
		if (other.getId() != id) {
			return false;
		}
		if (username == null && other.getUsername() != null 
				|| username != null && other.getUsername() == null) {
			return false;
		}
		if (username != null && !username.equals(other.getUsername())) {
			return false;
		}
		
		return true;
	}

}
