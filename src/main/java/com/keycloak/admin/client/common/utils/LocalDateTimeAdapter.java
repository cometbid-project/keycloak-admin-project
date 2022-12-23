/**
 * 
 */
package com.keycloak.admin.client.common.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Gbenga
 *
 */
public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

	static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public LocalDateTime unmarshal(String v) throws Exception {
		return LocalDateTime.parse(v, formatter);
	}

	public String marshal(LocalDateTime v) throws Exception {
		return v.toString();
	}

}
