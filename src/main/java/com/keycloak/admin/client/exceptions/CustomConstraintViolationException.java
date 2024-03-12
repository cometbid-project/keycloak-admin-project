/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.keycloak.admin.client.exceptions;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;

import com.keycloak.admin.client.common.utils.ResourceBundleAccessor;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Gbenga
 */
@Getter
@Setter
public class CustomConstraintViolationException extends ConstraintViolationException {

    /**
     *
     */  
    private static final long serialVersionUID = 4851700841344564891L;

    private static final HttpStatus STATUS = HttpStatus.UNPROCESSABLE_ENTITY;

    public CustomConstraintViolationException(Set<? extends ConstraintViolation<?>> constraintViolations) {
        super(constraintViolations);
        // TODO Auto-generated constructor stub
    }

    public HttpStatus getStatus() {
        return STATUS;
    }

    /**
     *
     */
    //@Override
    public String getErrorCode() {
        return ErrorCode.CONSTRAINT_VIOLATION_ERR_CODE.getErrCode();
    }
    
    /**
	 * 
	 */
	//@Override
	public String getErrorMessage() {
		String msgKey = ErrorCode.CONSTRAINT_VIOLATION_ERR_CODE.getErrMsgKey();
		return ResourceBundleAccessor.accessMessageInBundle(msgKey, new Object[] {});
	}
}
