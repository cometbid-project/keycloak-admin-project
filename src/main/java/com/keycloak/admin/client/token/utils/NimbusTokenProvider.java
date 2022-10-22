/**
 * 
 */
package com.keycloak.admin.client.token.utils;

import static com.keycloak.admin.client.config.AuthProperties.*;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.models.CustomAuthenticationDetails;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import com.nimbusds.jwt.SignedJWT;

import lombok.extern.log4j.Log4j2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Primary
@Component("nimbus")
public class NimbusTokenProvider implements TokenManager {

	private RSAKey privateKey;

	@Autowired
	private AuthProperties prop;

	private Date validity;
	private String AUDIENCE;
	private String ISSUER;

	@PostConstruct
	public void init() {
		AUDIENCE = prop.getLocalTokenAudience();
		ISSUER = prop.getIssuer();
	}

	public NimbusTokenProvider() throws Exception {
		privateKey = new RSAKeyGenerator(2048).keyID(UUID.randomUUID().toString()).generate();
	}

	@Override
	public String createJwtToken(Authentication authentication) {
		String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(","));

		Date currentTime = new Date();
		long expirationPeriod = prop.getTokenValidityInMinutes();
		validity = Date.from(Instant.now().plus(Duration.ofMinutes(expirationPeriod)));

		try {
			JWSSigner signer = new RSASSASigner(privateKey);

			Builder jwtBuilder = new JWTClaimsSet.Builder()
					// set the value of the issuer.
					.issuer(ISSUER)
					// set the subject value - JWT belongs to this subject.
					.subject(authentication.getName())
					// set values for audience restriction.
					.audience(AUDIENCE)
					// expiration time set to 10 minutes.
					.expirationTime(validity)
					// set the valid from time to current time.
					.notBeforeTime(currentTime)
					// set issued time to current time.
					.issueTime(currentTime)
					// set a generated UUID as the JWT identifier.
					.jwtID(UUID.randomUUID().toString())
					// add JWS Claims - role.
					.claim(AUTHORITIES_CLAIM, authorities);

			Object credentialDetails = authentication.getDetails();
			if (credentialDetails != null && credentialDetails instanceof CustomAuthenticationDetails) {
				CustomAuthenticationDetails authDetails = (CustomAuthenticationDetails) credentialDetails;

				// add JWS Claims - Subscription.
				jwtBuilder.claim(SUBSCRIPTION_CLAIM, authDetails.getSubscriptionType());
			}

			JWTClaimsSet jwtClaims = jwtBuilder.build();
			// create signer with the RSA private key..
			// create JWS header with RSA-SHA256 algorithm.
			JWSHeader jswHeader = new JWSHeader.Builder(JWSAlgorithm.RS256).build();
			// .keyID(privateKey.getKeyID())

			// create the signed JWT with the JWS header and the JWT body.
			SignedJWT signedJWT = new SignedJWT(jswHeader, jwtClaims);

			// sign the JWT with HMAC-SHA256.
			signedJWT.sign(signer);
			// serialize into base64url-encoded text.
			String token = signedJWT.serialize();
			return token;

		} catch (Exception ex) {
			return null;
		}
	}

	@Override
	public boolean validateToken(String token) {

		if (StringUtils.isBlank(token)) {
			log.info("Token not found");
			return false;
		}

		try {
			SignedJWT signedJWT = SignedJWT.parse(token);
			RSAPublicKey publicKey = privateKey.toRSAPublicKey();

			JWSVerifier verifier = new RSASSAVerifier(publicKey);

			boolean success = signedJWT.verify(verifier);
			if (success) {
				JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
				return validateClaims(claimsSet);
			} else {
				return false;
			}
		} catch (Exception ex) {
			return false;
		}
	}

	private boolean validateClaims(JWTClaimsSet localClaims) {

		Date now = new Date();
		Date expiratnDate = localClaims.getExpirationTime();

		if (now.after(expiratnDate)) {
			log.info("Expiration: {}/Now: {}", expiratnDate, now);
			return false;
		}

		String subject = localClaims.getSubject();
		List<String> aud = localClaims.getAudience();
		String issuer = localClaims.getIssuer();

		log.info("Username", subject);
		log.info("Audience: {}/{}", aud, AUDIENCE);
		log.info("Issuer: {}/{}", localClaims.getIssuer(), ISSUER);

		return StringUtils.isNotBlank(subject) && aud.contains(AUDIENCE) && ISSUER.equals(issuer);

	}

	public JWTClaimsSet extractClaims(String token) {
		SignedJWT signedJWT;
		try {
			signedJWT = SignedJWT.parse(token);
			RSAPublicKey publicKey = privateKey.toRSAPublicKey();

			JWSVerifier verifier = new RSASSAVerifier(publicKey);

			boolean success = signedJWT.verify(verifier);
			if (success) {
				return signedJWT.getJWTClaimsSet();
			}
			return null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("Invalid token", e);
			return null;
		}
	}

	/**
	 * 
	 */
	@Override
	public Authentication getAuthentication(String token) {

		Optional<JWTClaimsSet> optLocalClaims = Optional.ofNullable(extractClaims(token));

		if (optLocalClaims.isPresent()) {
			JWTClaimsSet localClaims = optLocalClaims.get();

			Collection<? extends GrantedAuthority> authorities = Arrays
					.stream(localClaims.getClaim(AUTHORITIES_CLAIM).toString().split(","))
					.map(SimpleGrantedAuthority::new).collect(Collectors.toList());

			// User principal = new User(localClaims.getSubject(), "", authorities);
			String principal = localClaims.getSubject();

			return new UsernamePasswordAuthenticationToken(principal, token, authorities);
		}
		return null;

	}

	public String buildEncryptedJWT(PublicKey publicKey, Authentication authentication) throws JOSEException {

		String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(","));

		// build audience restriction list.
		List<String> aud = new ArrayList<String>();
		aud.add(AUDIENCE);

		Date currentTime = new Date();

		// create a claims set.
		Builder jwtBuilder = new JWTClaimsSet.Builder()
				// set the value of the issuer.
				.issuer(ISSUER)
				// set the subject value - JWT belongs to this subject.
				.subject(authentication.getName())
				// set values for audience restriction.
				.audience(aud)
				// expiration time set to 10 minutes.
				.expirationTime(validity)
				// set the valid from time to current time.
				.notBeforeTime(currentTime)
				// set issued time to current time.
				.issueTime(currentTime)
				// set a generated UUID as the JWT identifier.
				.jwtID(UUID.randomUUID().toString())
				// create JWS header with RSA-SHA256 algorithm.
				.claim(AUTHORITIES_CLAIM, authorities);

		Object credentialDetails = authentication.getDetails();
		if (credentialDetails != null && credentialDetails instanceof CustomAuthenticationDetails) {
			CustomAuthenticationDetails authDetails = (CustomAuthenticationDetails) credentialDetails;

			// add JWS Claims - Subscription.
			jwtBuilder.claim(SUBSCRIPTION_CLAIM, authDetails.getSubscriptionType());
		}

		JWTClaimsSet jwtClaims = jwtBuilder.build();

		// create JWE header with RSA-OAEP and AES/GCM.
		JWEHeader jweHeader = new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM);

		// create encrypter with the RSA public key.
		JWEEncrypter encrypter = new RSAEncrypter((RSAPublicKey) publicKey);

		// create the encrypted JWT with the JWE header and the JWT payload.
		EncryptedJWT encryptedJWT = new EncryptedJWT(jweHeader, jwtClaims);

		// encrypt the JWT.
		encryptedJWT.encrypt(encrypter);

		// serialize into base64url-encoded text.
		String jwtInText = encryptedJWT.serialize();

		// print the value of the JWT.
		log.debug(jwtInText);

		return jwtInText;
	}

	@Override
	public String getSubscriptionType(String token) {
		// TODO Auto-generated method stub
		JWTClaimsSet localClaims = extractClaims(token);

		Optional<Object> opt = Optional.ofNullable(localClaims.getClaim(SUBSCRIPTION_CLAIM));
		if (opt.isPresent()) {
			return opt.get().toString();
		}

		return null;
	}

}
