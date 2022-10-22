/**
 * 
 */
package com.keycloak.admin.client.integration.enums;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.keycloak.admin.client.integration.messaging.factory.SESFactory;
import com.keycloak.admin.client.integration.service.SES;

/**
 * @author Gbenga
 *
 */
public enum SESFrom implements SES {

	ATTA {
		@Override
		public String getEmail() {

			return SESFactory.getInstance().getSupportEmail();
		}

		@Override
		public String getName() {

			return SESFactory.getInstance().getSupportName();
		}
	},
	NO_REPLY {
		@Override
		public String getEmail() {
			return SESFactory.getInstance().getNoReplyEmail();
		}

		@Override
		public String getName() {
			return SESFactory.getInstance().getNoReplyName();
		}
	},
	SUPPORT_TEAM {

		@Override
		public String getEmail() {

			return SESFactory.getInstance().getSupportEmail();
		}

		@Override
		public String getName() {

			return SESFactory.getInstance().getSupportName();
		}
	};

	public abstract String getEmail();

	public abstract String getName();

	@Override
	public String toString() {
		return this.getName();
	}

	// Implementing a fromString method on an enum type
	private static final Map<String, SESFrom> stringToEnum = new HashMap<String, SESFrom>();

	static { // Initialize map from constant name to enum constant
		for (SESFrom op : values()) {
			stringToEnum.put(op.toString(), op);
		}
	}

	// Returns Operation for string, or null if string is invalid
	public static SESFrom fromString(String typeName) {
		return stringToEnum.get(typeName);
	}

	public static Set<String> getAllTypes() {
		return stringToEnum.keySet();
	}
}
