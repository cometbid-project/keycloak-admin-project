/**
 * 
 */
package com.keycloak.admin.client.token.utils;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import lombok.RequiredArgsConstructor;
import static com.keycloak.admin.client.error.helpers.ErrorPublisher.*;
import static dev.samstevens.totp.util.Utils.getDataUriForImage;

import java.util.SplittableRandom;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import dev.samstevens.totp.recovery.RecoveryCodeGenerator;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;

/**
 * @author Gbenga
 *
 */
@Component("TotpManager")
@RequiredArgsConstructor
public class TotpManagerImpl implements TotpManager {

	@Value("totp.qrCode.issuer")
	private String ISSUER;
	
	@Autowired
	private Environment env;

	private final CustomMessageSourceAccessor i8nMessageAccessor;
	private final CodeVerifier codeVerifier;
	private final QrDataFactory qrDataFactory;
	private final QrGenerator qrGenerator;
	private final SecretGenerator secretGenerator;

	/**
	 * 
	 */
	@Override
	public String generateSecret() {
		return secretGenerator.generate();
	}

	/**
	 * 
	 */
	@Override
	public boolean validateCode(String code, String secret) {
		return codeVerifier.isValidCode(secret, code);
	}

	/**
	 * @throws QrGenerationException
	 * @Override
	 */
	@Override
	public String generateQrImage(String email, String secret) {
		// Generate and store the secret
		// String secret = secretGenerator.generate();
		String qrCodeImage = null;
		try {
			QrData data = qrDataFactory.newBuilder().label(email).secret(secret).issuer(ISSUER).build();

			byte[] imageData = qrGenerator.generate(data);
			String mimeType = qrGenerator.getImageMimeType();

			// Generate the QR code image data as a base64 string which
			// can be used in an <img> tag:
			qrCodeImage = getDataUriForImage(imageData, mimeType);
		} catch (Exception ex) {
			raiseRuntimeException(i8nMessageAccessor.getLocalizedMessage("qrImage.failed.error", new Object[] {}), ex);
		}

		return qrCodeImage;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public String generateOtp() {
		String configOtpLength = env.getProperty("totp.code.length");
		int otpLength = StringUtils.isNotBlank(configOtpLength) ? Integer.valueOf(configOtpLength) : 6; 
		
		SplittableRandom splittableRandom = new SplittableRandom();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < otpLength; ++i) {
			sb.append(splittableRandom.nextInt(0, 10));
		}

		return sb.toString();
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public String[] generateRecoveryCodes() {
		RecoveryCodeGenerator recoveryCodes = new RecoveryCodeGenerator();
		String[] codes = recoveryCodes.generateCodes(16);
		return codes;
	}
}
