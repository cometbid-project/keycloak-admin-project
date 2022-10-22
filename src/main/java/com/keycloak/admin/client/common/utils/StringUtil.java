/**
 * 
 */
package com.keycloak.admin.client.common.utils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * @author Gbenga
 *
 */
@Log4j2
public class StringUtil {

	public static final String HTTPS_URL_SCHEME = "https://";
	public static final String HTTP_URL_SCHEME = "http://";
	public static final Charset charset = StandardCharsets.US_ASCII;

	/**
	 * 
	 * @param str
	 * @return
	 */
	public static String removeWhiteSpaces(@NonNull final String str) {
		return str.replaceAll("\\s+", "");
	}

	/**
	 * 
	 * @param phoneNo
	 * @return
	 */
	public static String formatPhoneNo(@NonNull final String phoneNo) {
		String regrex = "[^\\d+]|(?!^)\\+";

		String formattedPhone = phoneNo.replaceAll(regrex, "");

		if (formattedPhone.startsWith("+") || formattedPhone.startsWith("0")) {
			formattedPhone = formattedPhone.substring(1);
		}

		log.info("Format Phone no {}", formattedPhone);
		return formattedPhone;
	}

	/**
	 *
	 * @param phoneNo
	 * @param dialCode
	 * @return
	 */
	public static String formatPhoneNo(@NonNull final String phoneNo, @NonNull final String dialCode) {
		String formattedPhone = formatPhoneNo(phoneNo);

		return dialCode.concat(formattedPhone);
	}

	/**
	 * 
	 * @param str
	 * @return
	 */
	public static byte[] encodeString(String str) {
		return charset.encode(str).array();
	}

	/**
	 * 
	 * @param byteArrray
	 * @return
	 */
	public static String decodeString(byte[] byteArrray) {
		return charset.decode(ByteBuffer.wrap(byteArrray)).toString();
	}

}
