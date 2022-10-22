/**
 * 
 */
package com.keycloak.admin.client.dataacess;

import java.security.KeyPair;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.keycloak.admin.client.models.UserVO;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;

/**
 * @author Gbenga
 *
 */
@Log4j2
public class JwtUtil {

	private long expirationPeriod = 30L;

	private static KeyPair keyPair;
	private static Date validity;
	private static String AUDIENCE;
	private static String ISSUER;

	private JwtUtil() {
		keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);

		validity = Date.from(Instant.now().plus(Duration.ofMinutes(expirationPeriod)));
		AUDIENCE = "app";
		ISSUER = "app";
	}

	public static JwtUtil instance() {
		return new JwtUtil();
	}

	/**
	 * Tries to parse specified String as a JWT token. If successful, returns User
	 * object with username, id and role prefilled (extracted from token). If
	 * unsuccessful (token is invalid or not containing all required user
	 * properties), simply returns null.
	 * 
	 * @param token the JWT token to parse
	 * @return the User object extracted from specified token or null if a token is
	 *         invalid.
	 */
	public UserVO parseToken(String token) {
		try {
			Claims body = (Claims) extractClaims(token);

			Set<String> roles = Collections.emptySet();
			roles.add((String) body.get("role"));

			UserVO u = new UserVO();
			u.setUsername(body.getSubject());
			u.setId((String) body.get("userId"));
			u.setRoles(roles);

			return u;
		} catch (JwtException | ClassCastException e) {
			return null;
		}
	}

	/**
	 * Generates a JWT token containing username as subject, and userId and role as
	 * additional claims. These properties are taken from the specified User object.
	 * 
	 * @param u the user for which the token will be generated
	 * @return the JWT token
	 */
	public String generateToken(UserVO u) {
		Claims claims = Jwts.claims().setSubject(u.getUsername());
		claims.put("userId", u.getId() + "");
		claims.put("role", u.getRoles());

		return Jwts.builder().setClaims(claims)
				//
				.signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
				// set the validity period.
				.setExpiration(validity)
				// set values for audience restriction.
				.setAudience(AUDIENCE)
				// set the value of the issuer
				.setIssuer(ISSUER)
				// set issued time to current time.
				.setIssuedAt(new Date())
				//
				.compact();
	}

	public static Claims extractClaims(String jwt) {

		return Jwts.parserBuilder()
				//
				.requireAudience(AUDIENCE)
				//
				.setSigningKey(keyPair.getPublic())
				//
				.build()
				//
				.parseClaimsJws(jwt)
				//
				.getBody();
	}

	/**
	 * 
	 */
	public boolean validateToken(String token) {

		if (StringUtils.isBlank(token)) {
			log.info("Token not found");
			return false;
		}

		Date now = new Date();
		Claims localClaims = extractClaims(token);

		Date expiratnDate = localClaims.getExpiration();

		if (now.after(expiratnDate)) {
			log.info("Expiration: {}/Now: {}", expiratnDate, now);
			return false;
		}

		try {

			String subject = localClaims.getSubject();
			String aud = localClaims.getAudience();
			String issuer = localClaims.getIssuer();

			log.info("Username", subject);
			log.info("Audience: {}/{}", aud, AUDIENCE);
			log.info("Issuer: {}/{}", issuer, ISSUER);

			return StringUtils.isNotBlank(subject) && AUDIENCE.contains(aud) && ISSUER.equals(issuer);

		} catch (MalformedJwtException e) {
			log.info("Invalid JWT token.");
			log.trace("Invalid JWT token trace: {}", e);
		} catch (ExpiredJwtException e) {
			log.info("Expired JWT token.");
			log.trace("Expired JWT token trace: {}", e);
		} catch (UnsupportedJwtException e) {
			log.info("Unsupported JWT token.");
			log.trace("Unsupported JWT token trace: {}", e);
		} catch (IllegalArgumentException e) {
			log.info("JWT token compact of handler are invalid.");
			log.trace("JWT token compact of handler are invalid trace: {}", e);
		}
		return false;
	}

}
