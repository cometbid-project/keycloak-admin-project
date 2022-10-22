/**
 * 
 */
package com.keycloak.admin.client.models.mappers;

import java.time.ZonedDateTime;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.IdGenerator;

import com.keycloak.admin.client.common.activity.ActivityLog;
import com.keycloak.admin.client.common.activity.ActivityLogVO;
import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.models.Username;

import lombok.NonNull;

/**
 * @author Gbenga
 *
 */
@Component
public class ActivityLogMapper {

	private final ModelMapper modelMapper;
	private final IdGenerator idGenerator;

	public ActivityLogMapper(ModelMapper modelMapper, IdGenerator idGenerator) {
		this.modelMapper = modelMapper;
		this.idGenerator = idGenerator;
	}

	public ActivityLog create(@NonNull String activityStmt, @NonNull Username currentUser, @NonNull String comment,
			ZonedDateTime timestamp) {

		ActivityLog activityLog = ActivityLog.builder().id(idGenerator.generateId().toString())
				.status(StatusType.VALID.toString()).username(currentUser).activityStmt(activityStmt).comment(comment)
				.timestamp(timestamp).build();

		return activityLog;
	}

	public ActivityLogVO toViewObject(@NonNull ActivityLog actLog) {

		ActivityLogVO activityResource = modelMapper.map(actLog, ActivityLogVO.class);
		return activityResource;
	}

}
