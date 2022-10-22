package com.keycloak.admin.client.config;

import com.maxmind.geoip2.DatabaseReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;
import ua_parser.Parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 
 * @author Gbenga
 *
 */
@Configuration
public class LoginNotificationConfig {

	@Bean
	Parser uaParser() throws IOException {
		return new Parser();
	}

	@Bean(name = "GeoIPCity")
	DatabaseReader databaseReader() throws IOException {
		File database = ResourceUtils.getFile("classpath:maxmind/GeoLite2-City.mmdb");
		return new DatabaseReader.Builder(database).build();
	}
	
}
