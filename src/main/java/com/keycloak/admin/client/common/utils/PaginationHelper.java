/**
 *
 */
package com.keycloak.admin.client.common.utils;

import java.util.Collections;
import java.util.List;
import lombok.extern.log4j.Log4j2;

/**
 * @author Gbenga
 *
 */
@Log4j2
public class PaginationHelper {

	public static int adjustPageNo(int pageNo) {
		// Adjust to ensure first page doesn't skip any record
		return pageNo - 1;
	}

	/**
	 * returns a view (not a new list) of the sourceList for the range based on page
	 * and pageSize
	 * 
	 * @param sourceList
	 * @param page,      page number should start from 1
	 * @param pageSize
	 * @return
	 */
	public static <T> List<T> getPage(List<T> sourceList, int page, int pageSize) {
		if (pageSize <= 0 || page <= 0) {
			// using default instead
			page = 1;
			pageSize = 10;
		}

		int fromIndex = page * pageSize;
		if (sourceList == null || sourceList.size() < fromIndex) {
			return Collections.emptyList();
		}

		// toIndex exclusive
		return sourceList.subList(fromIndex, Math.min(fromIndex + pageSize, sourceList.size()));
	}

}
