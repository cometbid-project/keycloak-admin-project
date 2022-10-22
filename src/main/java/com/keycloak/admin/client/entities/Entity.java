/**
 * 
 */
package com.keycloak.admin.client.entities;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Sort;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Gbenga
 *
 */
@Getter
@Setter
@ToString(includeFieldNames = true, callSuper = true)
public abstract class Entity {

	public static final String PERIOD_CHAR = ".";
	public static final String DEFAULT_SORT_FIELD = "id";
	//private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.by("id"));

	@Id
	@JsonIgnore
	@EqualsAndHashCode.Include
	@JsonProperty(DEFAULT_SORT_FIELD)   
	// @Field(name = "ID")
	protected String id;
}
