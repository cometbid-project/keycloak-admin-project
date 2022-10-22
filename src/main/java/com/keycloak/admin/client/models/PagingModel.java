/**
 * 
 */
package com.keycloak.admin.client.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import com.keycloak.admin.client.common.utils.PaginationHelper;

/**
 * @author Gbenga
 *
 */
@Data
@Builder
@ToString(includeFieldNames = true)
@NoArgsConstructor
@AllArgsConstructor
public class PagingModel {

	@Builder.Default
	private int pgNo = 1;

	@Builder.Default
	private int pgSize = 10;

	public static String getDefault() {
		int page = 1;
		int limit = 10;
		return "(" + page + "," + limit + ")";
	}

	public int getPgNo() {
		if (pgNo < 1) {
			pgNo = 1;
		}
		return PaginationHelper.adjustPageNo(pgNo);
	}

	public int getPgSize() {
		if (pgSize < 1) {
			pgSize = 10;
		}

		return pgSize;
	}

}
