/**
 * 
 */
package com.keycloak.admin.client.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort.Order;
import com.keycloak.admin.client.models.OrderingModel;

/**
 * @author Gbenga
 *
 */
class OrderingModelTest {

	@Test
	public void testDefaultSortOrderModel() {
		OrderingModel sortOrder = new OrderingModel();

		sortOrder.addOrderToList(Order.by("field1"));
		sortOrder.addOrderToList(Order.by("field2"));
		sortOrder.addOrderToList(Order.by("field3"));

		Set<Order> setOfSortOrder = sortOrder.getSortOrder();
		assertThat(!setOfSortOrder.isEmpty());
		assertThat(setOfSortOrder.size() == 3);

		sortOrder.createSortOrderOnField("field4");
		sortOrder.createSortOrderOnField("field1");
		assertThat(!setOfSortOrder.isEmpty());
		assertThat(setOfSortOrder.size() == 4);

		Map<String, String> mappedFields = new HashMap<>();
		mappedFields.put("field1", "a");
		mappedFields.put("field2", "b");
		mappedFields.put("field3", "c");
		mappedFields.put("field4", "d");
		mappedFields.put("field5", "e");

		Set<Order> mySortOrder = sortOrder.createMappedSortOrder(mappedFields);
		assertThat(!mySortOrder.isEmpty());
		assertThat(mySortOrder.size() == 4);
		assertThat(mySortOrder).contains(Order.by("a")).contains(Order.by("b")).contains(Order.by("c"))
				.contains(Order.by("d"));
	}
	
	@Test
	public void testSortOrderModel() {
		Set<Order> setOfSortOrder = new HashSet<>();
		setOfSortOrder.add(Order.by("field1"));
		setOfSortOrder.add(Order.by("field2"));
		setOfSortOrder.add(Order.by("field3"));
		
		OrderingModel sortOrder = new OrderingModel(setOfSortOrder);

		Set<Order> mySortOrder = sortOrder.getSortOrder();
		assertThat(!mySortOrder.isEmpty());
		assertThat(mySortOrder.size() == 3);

		sortOrder.createSortOrderOnField("field4");
		sortOrder.createSortOrderOnField("field1");
		assertThat(!setOfSortOrder.isEmpty());
		assertThat(setOfSortOrder.size() == 4);

		Map<String, String> mappedFields = new HashMap<>();
		mappedFields.put("field1", "a");
		mappedFields.put("field2", "b");
		mappedFields.put("field3", "c");
		mappedFields.put("field4", "d");
		mappedFields.put("field5", "e");

		Set<Order> aSortOrder = sortOrder.createMappedSortOrder(mappedFields);
		assertThat(!aSortOrder.isEmpty());
		assertThat(aSortOrder.size() == 4);
		assertThat(aSortOrder).contains(Order.by("a")).contains(Order.by("b")).contains(Order.by("c"))
				.contains(Order.by("d"));
	}
}
