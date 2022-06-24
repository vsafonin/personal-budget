package ru.vladimir.personalaccounter.service;

import java.util.Locale;
import java.util.Optional;

import javax.validation.Valid;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.exception.BadAppUserFormatException;

public interface UserService {
    
    AppUser findByUsername(String username);

    public AppUser save(AppUser user);
    
    public void register(AppUser user, Locale locale) throws BadAppUserFormatException;

    public AppUser findByEmail(String email);
    
    public AppUser getCurrentAppUserFromContextOrCreateDemoUser();

	void deleteUser(AppUser theUser);

	Optional<AppUser> findById(Long userId);
	
	void changeEmail(String newEmail, Locale locale);
	
	void generateJwtToken(AppUser theAppUser);

	void deleteJwt(long id, AppUser theAppUser);

	void recoverUserPassword(AppUser theAppUser, Locale locale);

	AppUser findByAppUserRecoveryModel(String token);

	void changePassword(@Valid AppUser appUser);

	void setLastLogin(AppUser theAppUser);

}
