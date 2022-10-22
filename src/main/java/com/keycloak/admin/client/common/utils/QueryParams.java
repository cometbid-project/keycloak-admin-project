/**
 * 
 */
package com.keycloak.admin.client.common.utils;

import com.keycloak.admin.client.models.OrderingModel;
import com.keycloak.admin.client.models.PagingModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Gbenga
 *
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryParams {

	//DateFilter dateFilter;
	//ObjectFilter objectFilter;
	OrderingModel orderModel;
	PagingModel pagingModel;
	//RangeFilter rangeFilter;
	//GroupingModel groupModel;
	//SearchCriteriaModel searchModel;
}
