package ru.vladimir.personalaccounter.service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ru.vladimir.personalaccounter.entity.ActivationEmail;
import ru.vladimir.personalaccounter.entity.ActivationUuid;
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.AppUserJwtToken;
import ru.vladimir.personalaccounter.entity.AppUserRecoveryPasswordModel;
import ru.vladimir.personalaccounter.entity.Authorities;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.enums.AauthoritiesEnum;
import ru.vladimir.personalaccounter.enums.TypeOfEmails;
import ru.vladimir.personalaccounter.event.EmailEvent;
import ru.vladimir.personalaccounter.exception.AppUserJwtTokenNotFoundExp;
import ru.vladimir.personalaccounter.exception.AuthContextIsNullExcp;
import ru.vladimir.personalaccounter.exception.BadAppUserFormatException;
import ru.vladimir.personalaccounter.methods.JwtProvider;
import ru.vladimir.personalaccounter.model.EmailModel;
import ru.vladimir.personalaccounter.repository.ActivationEmailRepository;
import ru.vladimir.personalaccounter.repository.AppUserJwtRepository;
import ru.vladimir.personalaccounter.repository.AppUserRecoveryPasswordRepository;
import ru.vladimir.personalaccounter.repository.RoleRepository;
import ru.vladimir.personalaccounter.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private AppUserRecoveryPasswordRepository appUserRecoveryPasswordRepository;

	@Autowired
	private ActivationEmailRepository activationEmailRepository;
	
	@Autowired
	private AppUserJwtRepository appUserJwtRepository;
	
	@Value("${root.link.app}")
	private String rootLinkApp;
	
	@Autowired
	private JwtProvider jwtProvider;
	
	
	private AppUser demoUser;
	
	@Override
	public AppUser findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	@Override
	public AppUser save(AppUser user) {
		return userRepository.save(user);
	}

	@Override
	public void register(AppUser user, Locale locale) throws BadAppUserFormatException {
		if (user == null)
			throw new BadAppUserFormatException("appUser is null");
		if (user.getEmail() == null || user.getEmail().isEmpty())
			throw new BadAppUserFormatException("Email is invalid");
		// create activation link
		UUID userUuid = UUID.randomUUID();
		ActivationUuid activationUuid = new ActivationUuid();
		activationUuid.setExpiredDate(ZonedDateTime.now().plusHours(24));
		activationUuid.setUuid(userUuid.toString());
		user.setActivationLink(activationUuid);
		// set user role
		Authorities userRole = roleRepository.findByName(AauthoritiesEnum.USER.getRoleName());
		if (userRole == null) {
			// save new role (our DB is empty)
			userRole = new Authorities();
			userRole.setName(AauthoritiesEnum.USER.getRoleName());
			roleRepository.save(userRole);
		}
		user.setRoles(Stream.of(userRole).collect(Collectors.toSet()));
		eventPublisher.publishEvent(new EmailEvent(this, performEmailModel(user, 
				user.getEmail(), 
				locale,
				activationUuid.getUuid(),
				"registration"),TypeOfEmails.REGISTRATION_EMAIL));
		user.setPassword(passwordEncoder.encode(user.getPassword()));
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		AppUser user = userRepository.findByUsername(username);
		if (user == null) {
			throw new UsernameNotFoundException("User not found");
		}
		return user;
	}

	@Override
	public AppUser findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public void deleteUser(AppUser theUser) {
		userRepository.delete(theUser);
	}

	@Override
	public Optional<AppUser> findById(Long userId) {
		return userRepository.findById(userId);

	}

	@Override
	public void changeEmail(String newEmail, Locale locale) {
		ActivationEmail activationEmail = new ActivationEmail();
		activationEmail.setEmail(newEmail);
		// get current auth user
		AppUser user = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (user == null) {
			throw new RuntimeException("User not found from current contex, but it inpossible");
		}
		
		String uuid = UUID.randomUUID().toString();
		
		activationEmail.setAppUser(user);
		activationEmail.setUuid(uuid);
		activationEmail.setExpiredDate(ZonedDateTime.now().plusHours(24));
		activationEmailRepository.save(activationEmail);
		
		// send email
		eventPublisher.publishEvent(new EmailEvent(this, performEmailModel(user, newEmail, locale, uuid, "email-changing"), TypeOfEmails.CHANGED_EMAIL));
	}

	private EmailModel performEmailModel(AppUser user, String email, Locale locale, String uuid, String path) {
		EmailModel emailModel = new EmailModel();
		emailModel.setEmailTo(email);
		Map<String, Object> parametrs = new HashMap<>();
		parametrs.put("displayname", user.getDisplayName());
		parametrs.put("activationLink", rootLinkApp + "/" + path + "/" + uuid);
		emailModel.setParametrs(parametrs);
		emailModel.setLocale(locale);
		return emailModel;
	}

	@Override
	public void generateJwtToken(AppUser theAppUser) {
		theAppUser.setAppUserJwtTokens(List.of(jwtProvider.generateToken(theAppUser)));
		save(theAppUser);
	}

	@Override
	public void deleteJwt(long id, AppUser theAppUser) {
		Optional<AppUserJwtToken> theAppUserJwtTokenOptional = appUserJwtRepository.findById(id);
		if (!theAppUserJwtTokenOptional.isPresent()) {
			throw new AppUserJwtTokenNotFoundExp();
		}
		
		theAppUser.setAppUserJwtTokens(theAppUser.getAppUserJwtTokens().stream().filter(j -> j.getId() == id).collect(Collectors.toList()));
		save(theAppUser);
		appUserJwtRepository.delete(theAppUserJwtTokenOptional.get());		
	}

	@Override
	public AppUser getCurrentAppUserFromContextOrCreateDemoUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			throw new AuthContextIsNullExcp("secturity not have auth yet");
		}
		if (auth.getName().equals("anonymousUser")) {
			return createDemoUser();
		}
		return (AppUser) auth.getPrincipal();
	}

	private AppUser createDemoUser() {
		if (demoUser != null) {
			return demoUser;
		}
		AppUser demoUser = findByUsername("DemoUser");
		if (demoUser == null) {
			demoUser = new AppUser();
			demoUser.setUsername("DemoUser");
			BankAccount demoBankAccount = new BankAccount();
			demoBankAccount.setAppUser(demoUser);
			demoBankAccount.setBalance(BigDecimal.valueOf(100));
			demoBankAccount.setCurrency(Currency.getInstance("RUB"));
			demoBankAccount.setName("demoBank");
			demoUser.setBankAccounts(Set.of(demoBankAccount));
			return save(demoUser);

		}
		
		return demoUser;
				
				
				
	}

	@Override
	public void recoverUserPassword(AppUser theAppUser, Locale locale) {
		//generate token
		String token = getRandomString(15);
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, 1);
		AppUserRecoveryPasswordModel model = new AppUserRecoveryPasswordModel();
		model.setAppUse(theAppUser);
		model.setExpiredDate(cal.getTime());
		model.setToken(token);
		//send email
		eventPublisher.publishEvent(new EmailEvent(this, performEmailModel(theAppUser, theAppUser.getEmail(), locale, token, "recover"), TypeOfEmails.PASSWORD_RECOVER_EMAIL));
		appUserRecoveryPasswordRepository.save(model);
	}

	private String getRandomString(int leghtStr) {
		 int leftLimit = 97; // letter 'a'
		    int rightLimit = 122; // letter 'z'
		    Random random = new Random();

		    return random.ints(leftLimit, rightLimit + 1)
		      .limit(leghtStr)
		      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
		      .toString();
		    

	}

	@Override
	public AppUser findByAppUserRecoveryModel(String token) {
		Optional<AppUserRecoveryPasswordModel> appUserRecoveryPasswordModelOptional = appUserRecoveryPasswordRepository.findByToken(token);
		if (appUserRecoveryPasswordModelOptional.isPresent()) {
			return appUserRecoveryPasswordModelOptional.get().getAppUse();
		}
		return null;
	}

	@Override
	public void changePassword(@Valid AppUser appUser) {
		appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
		save(appUser);		
	}

	@Override
	public void setLastLogin(AppUser theAppUser) {
		
		theAppUser.setLastLogin(ZonedDateTime.now());
		save(theAppUser);
	}

}
