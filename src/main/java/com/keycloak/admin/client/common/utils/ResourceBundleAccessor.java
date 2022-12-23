/**
 * 
 */
package com.keycloak.admin.client.common.utils;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.reactive.function.server.ServerRequest;

/**
 * @author Gbenga
 *
 */
public class ResourceBundleAccessor {

	private ResourceBundleAccessor() {

	}

	private static ReloadableResourceBundleMessageSource messageSource;

	static {
		messageSource = new ReloadableResourceBundleMessageSource();
		// messageSource.setBasename("classpath:business/messages");
		messageSource.setBasenames("classpath:validation/messages", "classpath:business/messages", "classpath:ValidationMessages");
		messageSource.setUseCodeAsDefaultMessage(true);
		messageSource.setDefaultEncoding("UTF-8");
		messageSource.setFallbackToSystemLocale(false);
	}

	public static String accessMessageInBundle(String messageKey, Object[] args) {
		return messageSource.getMessage(messageKey, args, LocaleContextUtils.getContextLocale());
	}

	/**
	 * 
	 * @param messageKey
	 * @param parameterName
	 * @param rejectedValue
	 * @param dataType
	 * @return
	 */
	public static String getLocalizedErrorMessage(String messageKey, Object parameterName, Object rejectedValue,
			String dataType) {

		return accessMessageInBundle(messageKey, new Object[] { parameterName, dataType, rejectedValue });
	}
}