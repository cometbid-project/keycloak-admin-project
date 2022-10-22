/**
 * 
 */
package com.keycloak.admin.client.common.activity;

import com.keycloak.admin.client.common.activity.enums.Action;
import com.keycloak.admin.client.common.activity.enums.ContentType;
import com.keycloak.admin.client.common.activity.enums.ObjectType;

/**
 * @author Gbenga
 *
 */
public class ActivityFactory {

	private static ActivityLog activityLog = new ActivityLog();

	private ActivityFactory() {

	}

	public static String createActivityStmt(String subject, Action action, ObjectType objectType, String actionReceiver,
			ContentType content, String transactionId) {

		// ActivityLog activityLog = new ActivityLog();
		ActivityLog.Activity activity = activityLog.new Activity();

		activity.setSubject(subject);
		activity.setAction(action);
		activity.setObjectType(objectType);
		activity.setActionReceiver(actionReceiver);
		activity.setContent(content);
		activity.setTransactionId(transactionId);

		return activity.getActivityStmt();
	}
}
