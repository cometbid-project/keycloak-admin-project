/**
 * 
 */
package com.keycloak.admin.client.common.utils;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import lombok.extern.log4j.Log4j2;

/**
 * @author Gbenga
 *
 */
@Log4j2
public class LocaleContextUtils {

	public static final String THREAD_CONTEXT_LOCALE_KEY = "locale";

	public static Locale getContextLocale() {

		return I18nUtils.getLocaleFromString(getContextLocaleAsString());
	}

	public static String getContextLocaleAsString() {
		String localeStr = ThreadContext.get(THREAD_CONTEXT_LOCALE_KEY);

		String contextLocaleString = StringUtils.isNotBlank(localeStr) ? localeStr : Locale.getDefault().getLanguage();
		log.info("User context locale {}", contextLocaleString);

		return contextLocaleString;
	}
}
