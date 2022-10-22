/**
 * 
 */
package com.keycloak.admin.client.components;

import java.util.Locale;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.keycloak.admin.client.common.utils.LocaleContextUtils;

/**
 * @author Gbenga
 *
 */
@Component
public class CustomMessageSourceAccessor {

	@Autowired
	@Qualifier("messageSource")
	private MessageSource messageSource;

	private MessageSourceAccessor messageAccessor;

	@Nullable
	private final Locale defaultLocale = Locale.US;

	@PostConstruct
	public void init() {
		messageAccessor = new MessageSourceAccessor(messageSource);
	}

	/**
	 * 
	 * @param messageKey
	 * @param args
	 * @return
	 */
	public String getLocalizedMessage(String messageKey, Object[] args) {

		return messageAccessor.getMessage(messageKey, args, LocaleContextUtils.getContextLocale());
	}

	/**
	 * 
	 * @param messageKey
	 * @return
	 */
	public String getLocalizedMessage(String messageKey) {

		return messageAccessor.getMessage(messageKey, new Object[] {}, LocaleContextUtils.getContextLocale());
	}

	/**
	 * 
	 * @return
	 */
	public Locale getDefaultLocale() {
		return (this.defaultLocale != null ? this.defaultLocale : LocaleContextUtils.getContextLocale());
	}
}
