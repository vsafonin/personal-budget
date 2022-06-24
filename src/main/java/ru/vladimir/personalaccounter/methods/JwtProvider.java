package ru.vladimir.personalaccounter.methods;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.AppUserJwtToken;

@Component
@Slf4j
public class JwtProvider {
	
	@Value("${jwt.token.pass}")
	private String tokenPass;
	
	public AppUserJwtToken generateToken(AppUser theAppUser) {
		
		Date expDate = Date.from(LocalDate.now().plusMonths(3).atStartOfDay(ZoneId.systemDefault()).toInstant());

		String jwtToken = Jwts.builder()
				.setSubject(theAppUser.getUsername())
				.setExpiration(expDate)
				.signWith(SignatureAlgorithm.HS512, tokenPass)
				.compact();
		
		AppUserJwtToken theAppUserJwtToken = new AppUserJwtToken();
		theAppUserJwtToken.setAppUser(theAppUser);
		theAppUserJwtToken.setToken(jwtToken);
		theAppUserJwtToken.setExpDate(expDate);
		
		
		return theAppUserJwtToken; 
	}
	
	public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(tokenPass).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException expEx) {
            log.warn("Token expired");
        } catch (UnsupportedJwtException unsEx) {
            log.warn("Unsupported jwt");
        } catch (MalformedJwtException mjEx) {
            log.warn("Malformed jwt");
        } catch (SignatureException sEx) {
            log.warn("Invalid signature");
        } catch (Exception e) {
            log.warn("invalid token");
        }
        return false;
    }

    public String getLoginFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(tokenPass).parseClaimsJws(token).getBody();
        return claims.getSubject();
    }
	
}
