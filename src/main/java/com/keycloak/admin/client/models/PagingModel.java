/**
 * 
 */
package com.keycloak.admin.client.models;

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
public class PagingModel {

	private int pageNo;
	private int pageSize;

	public static String getDefault() {
		int page = 1;
		int limit = 10;
		return "(" + page + "," + limit + ")";
	}

	public int getPageNo() {
		return pageNo - 1;
	}

	public int getPageSize() {
		return pageSize;
	}

	/**
	 * @param pageNo
	 * @param pageSize
	 */
	public PagingModel(int pageNo, int pageSize) {
		super();
		this.pageNo = pageNo;
		this.pageSize = pageSize;
	}

	/**
	 * 
	 */
	public PagingModel() {
		super();
		this.pageNo = 1;
		this.pageSize = 10;
	}

}
