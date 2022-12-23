/**
 * 
 */
package com.keycloak.admin.client.models;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import javax.xml.bind.annotation.XmlAccessType;
import lombok.Data;

/**
 * @author Gbenga
 *
 */
@Data
@XmlRootElement(name = "users")
@XmlSeeAlso({ArrayList.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class ObjectWithList<T> {

	@XmlElement(name = "user")
	private List<T> list = null;

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

}
