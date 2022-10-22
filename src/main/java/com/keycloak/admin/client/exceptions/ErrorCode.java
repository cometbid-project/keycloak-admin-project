/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.keycloak.admin.client.exceptions;

/**
 *
 * @author Gbenga
 */
public interface ErrorCode {

	public static final String SYS_DEFINED_ERR_CODE = "SYSTEM-ERR";
	public static final String APP_DEFINED_ERR_CODE = "APP-DEF-001";
	public static final String AUTHENTICATION_ERR_CODE = "AUTH-ERR-001";
	public static final String BAD_REQUEST_ERR_CODE = "BAD-REQ-001";
	public static final String CONSTRAINT_VIOLATION_ERR_CODE = "INV-DATA-001";
	public static final String EVENT_PROCESSING_ERR_CODE = "EVENT-001";
	public static final String IMAGE_CONVERSION_ERR_CODE = "IMG-CONV-001";
	public static final String INVALID_PARAMETER_ERR_CODE = "INV-PARAM-001";
	public static final String MAX_LOGIN_ATTEMPT_ERR_CODE = "MAX-LOGIN-ATT-001";
	public static final String GENERIC_NOT_FOUND_ERR_CODE = "GEN-NF-001";
	public static final String INVALID_PASSWORD_ERR_CODE = "INV-PASWD-001";
	public static final String UNAUTHENTICATED_REQUEST_ERR_CODE = "UN-AUTH-001";
	public static final String UNAUTHORIZED_ACCESS_ERR_CODE = "UN-ACCESS-001";
	public static final String INVALID_ACTIVATION_TOKEN_ERR_CODE = "INV-ATOKEN-001";
	public static final String INVALID_JWT_TOKEN_ERR_CODE = "INV-JTOKEN-001";
	public static final String UNAUTHENTICATED_USER_ERR_CODE = "USR-UNAUTH-001";
	public static final String UNUSUAL_LOCATION_ERR_CODE = "UN-LOC-001";
	public static final String INVALID_ACCOUNT_ERR_CODE = "INV-ACCT-001";
	public static final String UNVERIFIED_ACCOUNT_ERR_CODE = "UNV-ACCT-001";
	public static final String ACCT_TYPE_NOT_FOUND_ERR_CODE = "ACTYP-NF-001";
	public static final String INACTIVE_MERCHANT_ERR_CODE = "NA-M-001";
	public static final String INACTIVE_ACCOUNT_ERR_CODE = "NA-ACCT-001";
	public static final String INACTIVE_PROFILE_ERR_CODE = "NA-USR-001";
	public static final String INSUFFICIENT_FUND_ERR_CODE = "INV-FUND-001";
	public static final String MAX_MEMBER_ALLOWED_ERR_CODE = "MAX-MEM-001";
	public static final String MAX_USER_ALLOWED_ERR_CODE = "MAX-USR-001";
	public static final String MEMBER_EXIST_ERR_CODE = "MEM-EXIST-001";
	public static final String MERCHANT_EXIST_ERR_CODE = "M-EXIST-001";
	public static final String MERCHANT_NOT_FOUND_ERR_CODE = "M-NF-001";
	public static final String ACCOUNT_NOT_FOUND_ERR_CODE = "ACCT-NF-001";
	public static final String USER_EXIST_ERR_CODE = "USR-EXIST-001";
	public static final String USER_NOT_FOUND_ERR_CODE = "USR-NF-001";
	public static final String EXPIRED_PROFILE_ERR_CODE = "USR-EXP-001";
	public static final String LOCKED_PROFILE_ERR_CODE = "USR-LOCK-001";
	public static final String UNVERIFIED_PROFILE_ERR_CODE = "USR-UNV-001";
	public static final String REQUEST_CONNECT_TIMEOUT_ERR_CODE = "TIMEOUT-001";
	public static final String UNAVAILABLE_SERVICE_ERR_CODE = "UN-SERV-001";
	public static final String INVALID_NEWLOCATION_TOKEN_ERR_CODE = "INV-LOCTOKEN-001";
	public static final String RESOURCE_EXIST_ERR_CODE = "RSC-EXIST-001";

	/**
	 * Provides an app-specific error code to help find out exactly what happened.
	 * It's a human-friendly identifier for a given exception.
	 *
	 * @return a short text code identifying the error
	 */
	String getErrorCode();
}
