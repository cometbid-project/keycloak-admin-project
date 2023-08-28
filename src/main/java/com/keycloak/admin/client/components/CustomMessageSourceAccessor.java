/**
 * 
 */
package com.keycloak.admin.client.components;

import java.util.Locale;

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
public class CustomMessageSourceAccessor extends MessageSourceAccessor {

	@Nullable
	private final Locale defaultLocale = Locale.US;

	public CustomMessageSourceAccessor(@Qualifier("messageSource") MessageSource messageSource) {
		super(messageSource);
	}

	/**
	 * 
	 * @param messageKey
	 * @param args
	 * @return
	 */
	public String getLocalizedMessage(String messageKey, Object[] args) {

		return this.getMessage(messageKey, args, LocaleContextUtils.getContextLocale());
	}

	/**
	 * 
	 * @param messageKey
	 * @return
	 */
	public String getLocalizedMessage(String messageKey) {

		return this.getMessage(messageKey, new Object[] {}, LocaleContextUtils.getContextLocale());
	}

	/**
	 * 
	 * @return
	 */
	public Locale getDefaultLocale() {
		return (this.defaultLocale != null ? this.defaultLocale : LocaleContextUtils.getContextLocale());
	}
}
