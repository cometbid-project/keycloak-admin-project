/**
 * 
 */
package com.keycloak.admin.client.common.utils;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;

import com.keycloak.admin.client.common.enums.SpecialCharacterData;

import lombok.NonNull;

/**
 * @author Gbenga
 *
 */
public class RandomGenerator {

	private static SecureRandom secureRandom = new SecureRandom();
	private static Random globalRandom = new Random();

	private static final int[] passwordLengths = { 8, 9, 10, 11, 15 };

	private final static int[] lengthArray = { 66, 72, 67, 77, 55 };

	private static final int[] sessionIdLengths = { 78, 43, 99, 88, 50, 90 };

	/**
	 * Generate a random string.
	 */
	public String nextString() {
		for (int idx = 0; idx < buf.length; ++idx)
			buf[idx] = symbols[random.nextInt(symbols.length)];
		return new String(buf);
	}

	public static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static final String lower = upper.toLowerCase(Locale.ROOT);

	public static final String digits = "0123456789";

	public static final String specialXter = "!@#$%^&*()_+";

	public static final String alphanum = upper + lower + digits;

	public static final String alphanumSpecial = upper + lower + digits + specialXter;

	private final Random random;

	private final char[] symbols;

	private final char[] buf;

	public RandomGenerator(int length, Random random, String symbols) {
		if (length < 1)
			throw new IllegalArgumentException();
		if (symbols.length() < 2)
			throw new IllegalArgumentException();
		this.random = Objects.requireNonNull(random);
		this.symbols = symbols.toCharArray();
		this.buf = new char[length];
	}

	/**
	 * Create an alphanumeric string generator.
	 */
	public RandomGenerator(int length, Random random) {
		this(length, random, alphanum);
	}

	/**
	 * Create an alphanumeric strings from a secure generator.
	 */
	public RandomGenerator(int length) {
		this(length, new SecureRandom());
	}

	/**
	 * Create session identifiers.
	 */
	public RandomGenerator() {
		this(21);
	}

	public static String randomCode(int length) {
		return new RandomGenerator(length).nextString();
	}

	/**
	 * 
	 * @return
	 */
	public static String generateUnique64LengthReferenceNo() {
		return generateSecureRandomHexToken(32);
	}

	/**
	 * 
	 * @param length
	 * @param useLetters
	 * @param useNumbers
	 * @return
	 */
	public static String generateRandomStringBounded(int length, boolean useLetters, boolean useNumbers) {

		return RandomStringUtils.random(length, useLetters, useNumbers);
	}

	/**
	 * 
	 * @param length
	 * @param useLetters
	 * @param useNumbers
	 * @return
	 */
	public static String generate6RandomDigits() {

		boolean useLetters = false;
		boolean useNumbers = true;
		int noOfDigits = 6;
		return RandomStringUtils.random(noOfDigits, useLetters, useNumbers);
	}

	/**
	 * 
	 * @param length
	 * @return
	 */
	public static String generateRandomAlphabeticString(int length) {

		return RandomStringUtils.randomAlphabetic(length);
	}

	/**
	 * Final length is twice byteLength
	 * 
	 * @param byteLength
	 * @return
	 */
	public static String generateSecureRandomHexToken(int byteLength) {
		byte[] token = new byte[byteLength];
		secureRandom.nextBytes(token);

		return new BigInteger(1, token).toString(16); // hex encoding
	}

	/**
	 * 
	 * @return
	 */
	public static String generateUniqueRefId() {
		String uuid = UUID.randomUUID().toString();
		return uuid.replace("-", "");
	}
	
	public static String generateTransactionId(@NonNull String timestamp) {
		return generate6RandomDigits() + "" + timestamp;
	}

	/**
	 * 
	 * @param length
	 * @return
	 */
	public static String getRandomStringWithSpecialCharacters(int length) {

		return new RandomGenerator(length, globalRandom, alphanumSpecial).nextString();
	}

	public static String getRandomAlphabet() {

		int index = globalRandom.nextInt(upper.length());
		String aChar = String.valueOf(upper.charAt(index));

		return aChar;
	}

	public static String generateRandomPassword() {

		List<CharacterRule> rules = Arrays.asList(new CharacterRule(EnglishCharacterData.UpperCase, 1),
				new CharacterRule(EnglishCharacterData.LowerCase, 1), new CharacterRule(EnglishCharacterData.Digit, 1),
				new CharacterRule(SpecialCharacterData.Special, 1));

		PasswordGenerator generator = new PasswordGenerator();
		String password = generator.generatePassword(randomLength(), rules);
		return password;
	}

	private static int randomLength() {
		return passwordLengths[globalRandom.nextInt(passwordLengths.length)];
	}

	public static String generateNewToken() {
		int length = lengthArray[globalRandom.nextInt(lengthArray.length)];
		final String newToken = randomCode(length);

		return newToken;
	}

	public static String generateSessionId() {
		int length = sessionIdLengths[globalRandom.nextInt(sessionIdLengths.length)];

		return randomCode(length);
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 */
	public static void main(String args[]) {
		System.out.println("Random refno = " + generateUnique64LengthReferenceNo());

		System.out.println("Random String = " + generateSecureRandomHexToken(13).length());

		System.out.println("Random hex String = " + getRandomStringWithSpecialCharacters(30));

	}
}
