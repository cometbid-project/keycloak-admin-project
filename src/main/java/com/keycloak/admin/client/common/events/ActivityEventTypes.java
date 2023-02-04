/**
 * 
 */
package com.keycloak.admin.client.common.events;

/**
 * @author Gbenga
 *
 */
public interface ActivityEventTypes {

	public static final String SEARCH_BRANCH_USER_EVENT = "SearchedBranchProfileEvent";
	public static final String SEARCH_BRANCH_TRANSACTION_EVENT = "SearchedTransactionLogEvent";
	public static final String UPDATE_BRANCH_USER_EVENT = "UserProfileUpdatedEvent";
	public static final String CREATE_BRANCH_USER_EVENT = "UserProfileCreatedEvent";
	public static final String UPDATE_USER_IMAGE_EVENT = "UserImageChangedEvent";
	public static final String UPDATE_USER_NEXOFKIN_EVENT = "UpdateUserNextOfKinEvent";

	public static final String ACTIVATION_TOKEN_CREATED_EVENT = "ActivationTokenCreatedEvent";
	public static final String ACTIVATION_TOKEN_RENEWED_EVENT = "ActivationTokenUpdatedEvent";
	public static final String AUTH_PROFILE_CREATED_EVENT = "AuthProfileCreatedEvent";
	public static final String SEARCH_AUTH_PROFILE_EVENT = "SearchAuthProfileEvent";
	public static final String UPDATE_AUTH_PROFILE_EVENT = "UpdateAuthProfileEvent";
	public static final String USER_AUTHENTICATION_EVENT = "UserAuthenticationEvent";
	public static final String USER_PASSWORD_CHANGED_EVENT = "UserPasswordChangedEvent";

	public static final String SEARCH_BRANCH_MEMBER_EVENT = "SearchMemberProfileEvent";
	public static final String UPDATE_BRANCH_MEMBER_EVENT = "UpdateBranchMemberEvent";
	public static final String BRANCH_ADDRESS_CHANGED_EVENT = "UpdateBranchAddressEvent";
	public static final String BRANCH_PHONE_CHANGED_EVENT = "UpdateBranchPhoneEvent";
	public static final String BRANCH_ADDRESS_REMOVED_EVENT = "RemoveBranchAddressEvent";
	public static final String MEMBER_ADDRESS_CHANGED_EVENT = "UpdateMemberAddressEvent";
	public static final String MEMBER_PHONE_CHANGED_EVENT = "UpdateMemberPhoneEvent";
	public static final String MEMBER_ADDRESS_REMOVED_EVENT = "RemoveMemberAddressEvent";
	public static final String MEMBER_PHONE_REMOVED_EVENT = "RemoveMemberPhoneEvent";
	public static final String UPDATE_MEMBER_NEXOFKIN_EVENT = "UpdateMemberNextOfKinEvent";
	public static final String UPDATE_MEMBER_IMAGE_EVENT = "UpdateMemberImageEvent";
	public static final String CREATE_BRANCH_MEMBER_EVENT = "CreateMemberProfileEvent";
	public static final String SEARCH_BRANCH_EVENT = "SearchedBranchProfileEvent";
	public static final String CREATE_BRANCH_EVENT = "CreateBranchProfileEvent";
	public static final String UPDATE_BRANCH_EVENT = "UpdateBranchProfileEvent";
	public static final String CHANGE_MERCHANT_SUBSCRIPTION = "UpdateMerchantSubscriptionEvent";
	public static final String BRANCH_SETTINGS_CHANGED_EVENT = "UpdateBranchSettingsEvent";
	public static final String BRANCH_PHONE_REMOVED_EVENT = "RemoveBranchPhoneEvent";
	public static final String ACCOUNT_TYPES_CHANGED_EVENT = "UpdateAccountTypesEvent";
	public static final String UPDATE_BRANCH_STATUS_EVENT = "UpdateBranchStatusEvent";
	public static final String CHANGE_PARENT_BRANCH_EVENT = "ChangeParentBranchEvent";
	public static final String MEMBER_PROFILE_CREATED_EVENT = "CreateMemberProfileEvent";
	public static final String MERCHANT_PROFILE_CREATED_EVENT = "CreateMerchantProfileEvent";
	public static final String PASSWORD_RESET_EVENT = "UserAuthPasswordResetEvent";
	public static final String UPDATE_MERCHANT_LOGO_EVENT = "UpdateMerchantLogoEvent";
	public static final String MEMBER_EMAIL_CHANGED_EVENT = "ChangeMemberEmailEvent";
	public static final String BRANCH_EMAIL_CHANGED_EVENT = "ChangeBranchEmailEvent";
	public static final String MEMBER_ALERT_OPTIONS_CHANGED_EVENT = "ChangeMemberAlertOptionsEvent";
	public static final String MEMBER_SOCIAL_PROFILE_CHANGED_EVENT = "ChangeMemberSocialProfileLinksEvent";
	public static final String DELETE_MERCHANT_EVENT = "RemoveMerchantEvent";
	public static final String CREATE_REALM_GROUP_EVENT = "CreateRealmGroupEvent";  
	public static final String SEARCH_REALM_GROUP_EVENT = "SearchRealmGroupEvent";
	public static final String CREATE_REALM_ROLE_EVENT = "CreateRealmRoleEvent";
	public static final String CREATE_CLIENT_ROLE_EVENT = "CreateClientRoleEvent";
	public static final String SEARCH_REALM_ROLE_EVENT = "SearchRealmRoleEvent";
	public static final String SEARCH_CLIENT_ROLE_EVENT = "SearchClientRoleEvent";
	public static final String CREATE_COMPOSITE_ROLE_EVENT = "CreateCompositeRealmRoleEvent";
	public static final String USER_LOCATION_SAVED_EVENT = "UserLocationSavedEvent";
	public static final String DELETE_USER_EVENT = "DeleteUserProfileEvent";  
	
}
