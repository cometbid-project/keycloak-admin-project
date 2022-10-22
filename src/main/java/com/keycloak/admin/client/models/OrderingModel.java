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

/**
 * @author Gbenga
 *
 */
@Data
@Builder
@ToString(includeFieldNames = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderingModel {

	@Builder.Default
	private Set<Order> sortOrder = new HashSet<>();

	public boolean addOrderToList(Order order) {

		Set<Order> newSet = new HashSet<>();
		newSet.add(order);

		return sortOrder.addAll(newSet);
	}

	public Set<Order> createDefaultSortOrder(String field) {
		// TODO Auto-generated method stub

		addOrderToList(Order.by(field));
		return sortOrder;
	}

	public Set<Order> createMappedSortOrder(Map<String, String> mappedFields) {
		// TODO Auto-generated method stub

		if (sortOrder == null) {
			return new HashSet<Order>();
		}

		sortOrder = sortOrder.stream().map(order -> {
			order.getProperty();
			String property = mappedFields.get(order.getProperty());

			if (StringUtils.isNotBlank(property)) {
				return order.withProperty(property);
			}
			return null;

		}).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());

		return sortOrder;
	}

}
