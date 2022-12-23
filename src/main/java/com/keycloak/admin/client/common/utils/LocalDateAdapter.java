/**
 * 
 */
package com.keycloak.admin.client.common.utils;

import java.time.LocalDate;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Gbenga
 *
 */
public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

    public LocalDate unmarshal(String v) throws Exception {
        return LocalDate.parse(v);
    }

    public String marshal(LocalDate v) throws Exception {
        return v.toString();
    }

}
