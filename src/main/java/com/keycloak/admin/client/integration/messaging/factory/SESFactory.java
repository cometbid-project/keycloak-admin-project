/**
 * 
 */
package com.keycloak.admin.client.integration.messaging.factory;

import java.util.Properties;

import javax.annotation.PostConstruct;
import org.springframework.util.ResourceUtils;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;

/**
 * @author Gbenga
 *
 */
@Log4j2
public class SESFactory {

	private static SESFactory me;

	private Properties properties;

	private SESFactory() {

	}

	public static SESFactory getInstance() {
		if (me == null) {
			me = new SESFactory();
		}

		me.properties = fetchProperties();
		return me;
	}

	public static Properties fetchProperties() {
		Properties properties = new Properties();
		try {
			File file = ResourceUtils.getFile("classpath:SESConfiguration.properties");
			InputStream in = new FileInputStream(file);
			properties.load(in);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return properties;
	}

	public String getSupportEmail() {

		return (String) properties.get("support.team.email");
	}

	public String getNoReplyEmail() {

		return (String) properties.get("noreply.email");
	}

	public String getSupportName() {

		return (String) properties.get("support.team.name");
	}

	public String getNoReplyName() {

		return (String) properties.get("noreply.name");
	}

}
