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
public enum ErrorCode {

	SYS_DEFINED_ERR_CODE("SYSTEM-ERR", "sys.def.error"), APP_DEFINED_ERR_CODE("APP-DEF-001", "app.def.error"),
	AUTHENTICATION_ERR_CODE("AUTH-ERR-001", "auth.error"), BAD_REQUEST_ERR_CODE("BAD-REQ-001", "bad.req.error"),
	CONSTRAINT_VIOLATION_ERR_CODE("INV-DATA-001", "data.val.error"),
	EVENT_PROCESSING_ERR_CODE("EVENT-001", "event.error"), IMAGE_CONVERSION_ERR_CODE("IMG-CONV-001", "img.con.error"),
	INVALID_PARAMETER_ERR_CODE("INV-PARAM-001", "inv.params.error"),
	INVALID_INPUT_ERR_CODE("INV-INPUT-001", "inv.input.error"),
	MAX_LOGIN_ATTEMPT_ERR_CODE("MAX-LOGIN-ATT-001", "login.attempt.error"),
	GENERIC_NOT_FOUND_ERR_CODE("GEN-NF-001", "not.found.error"),
	INVALID_PASSWORD_ERR_CODE("INV-PASWD-001", "invalid.password"),
	UNAUTHENTICATED_REQUEST_ERR_CODE("UN-AUTH-001", "unath.req.error"),
	UNAUTHORIZED_ACCESS_ERR_CODE("UN-ACCESS-001", "unathorized.error"),
	INVALID_ACTIVATION_TOKEN_ERR_CODE("INV-ATOKEN-001", "activation.token.err"),
	INVALID_JWT_TOKEN_ERR_CODE("INV-JTOKEN-001", "jwt.token.err"),
	UNAUTHENTICATED_USER_ERR_CODE("USR-UNAUTH-001", "unath.user.err"),
	UNUSUAL_LOCATION_ERR_CODE("UN-LOC-001", "unknown.loc.err"),
	INVALID_ACCOUNT_ERR_CODE("INV-ACCT-001", "inv.account.err"),
	UNVERIFIED_ACCOUNT_ERR_CODE("UNV-ACCT-001", "unverified.acct.err"),
	ACCT_TYPE_NOT_FOUND_ERR_CODE("ACTYP-NF-001", "acct.type.not.found"),
	INACTIVE_MERCHANT_ERR_CODE("NA-M-001", "inactive.merchant.err"),
	INACTIVE_ACCOUNT_ERR_CODE("NA-ACCT-001", "inactive.acct.err"),
	INACTIVE_PROFILE_ERR_CODE("NA-USR-001", "inactive.profile.err"),
	INSUFFICIENT_FUND_ERR_CODE("INS-FUND-001", "ins.fund.err"),
	MAX_MEMBER_ALLOWED_ERR_CODE("MAX-MEM-001", "max.member.allowed"),
	MAX_USER_ALLOWED_ERR_CODE("MAX-USR-001", "max.user.allowed"),
	MEMBER_EXIST_ERR_CODE("MEM-EXIST-001", "member.exist.err"),
	MERCHANT_EXIST_ERR_CODE("M-EXIST-001", "merchant.exist.err"),
	MERCHANT_NOT_FOUND_ERR_CODE("M-NF-001", "merchant.not.found"),
	ACCOUNT_NOT_FOUND_ERR_CODE("ACCT-NF-001", "account.not.found"),
	USER_EXIST_ERR_CODE("USR-EXIST-001", "user.exist.err"), USER_NOT_FOUND_ERR_CODE("USR-NF-001", "user.not.found"),
	EXPIRED_PROFILE_ERR_CODE("USR-EXP-001", "profile.expired"),
	LOCKED_PROFILE_ERR_CODE("USR-LOCK-001", "profile.locked"),
	UNVERIFIED_PROFILE_ERR_CODE("USR-UNV-001", "profile.unverified"),
	REQUEST_CONNECT_TIMEOUT_ERR_CODE("TIMEOUT-001", "connect.timeout.err"),
	UNAVAILABLE_SERVICE_ERR_CODE("UN-SERV-001", "unavailable.service"),
	INVALID_NEWLOCATION_TOKEN_ERR_CODE("INV-LOCTOKEN-001", "inv.location.token"),
	RESOURCE_EXIST_ERR_CODE("RSC-EXIST-001", "resource.exist.err"),

	HTTP_MEDIATYPE_NOT_SUPPORTED("HTTP-ERR-0002", "media.type.unsupported"),
	HTTP_MESSAGE_NOT_WRITABLE("HTTP-ERR-0003", "request.unwritable"),
	HTTP_MEDIA_TYPE_NOT_ACCEPTABLE("HTTP-ERR-0004", "media.type.unacceptable"),
	JSON_PARSE_ERROR("HTTP-ERR-0005", "json.parser.err"),
	HTTP_MESSAGE_NOT_READABLE("HTTP-ERR-0006", "request.unreadable");

	private String errCode;
	private String errMsgKey;

	ErrorCode(final String errCode, final String errMsgKey) {
		this.errCode = errCode;
		this.errMsgKey = errMsgKey;
	}

	/**
	 * Provides an app-specific error code to help find out exactly what happened.
	 * It's a human-friendly identifier for a given exception.
	 *
	 * @return a short text code identifying the error
	 */
	public String getErrCode() {
		return errCode;
	}

	/**
	 * @return the errMsgKey
	 */
	public String getErrMsgKey() {
		return errMsgKey;
	}

}
