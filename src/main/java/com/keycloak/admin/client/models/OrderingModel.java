/**
 * 
 */
package com.keycloak.admin.client.models;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Value;

/**
 * @author Gbenga
 *
 */
@Value
@Builder
@ToString(includeFieldNames = true)
//@NoArgsConstructor
//@AllArgsConstructor
public class OrderingModel {

	private Set<Order> sortOrder;

	public boolean addOrderToList(Order order) {
		return sortOrder.add(order);
	}

	public boolean createSortOrderOnField(String field) {

		return addOrderToList(Order.by(field));
	}

	public Set<Order> createMappedSortOrder(Map<String, String> mappedFields) {
		// TODO Auto-generated method stub

		return this.sortOrder.stream().map(order -> {
			// order.getProperty();
			String field = mappedFields.get(order.getProperty());

			return StringUtils.isNotBlank(field) ? order.withProperty(field) : null;

		}).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
	}

	/**
	 * @param sortOrder
	 */
	public OrderingModel(Set<Order> sortOrder) {
		super();
		this.sortOrder = sortOrder;
	}

	/**
	 * 
	 */
	public OrderingModel() {
		this(new HashSet<>());
	}

}
