/**
 * 
 */
package com.keycloak.admin.client.common.enums;

import org.passay.CharacterData;

/**
 * @author Gbenga
 *
 */
public enum SpecialCharacterData implements CharacterData {

	/** Special characters. */
	Special("INSUFFICIENT_SPECIAL", // ASCII symbols
			"@#$&_");

	/** Error code. */
	private final String errorCode;

	/** Characters. */
	private final String characters;

	/**
	 * Creates a new english character data.
	 *
	 * @param code       Error code.
	 * @param charString Characters as string.
	 */
	SpecialCharacterData(final String code, final String charString) {
		errorCode = code;
		characters = charString;
	}

	@Override
	public String getErrorCode() {
		// TODO Auto-generated method stub
		return errorCode;
	}

	@Override
	public String getCharacters() {
		// TODO Auto-generated method stub
		return characters;
	}

}
