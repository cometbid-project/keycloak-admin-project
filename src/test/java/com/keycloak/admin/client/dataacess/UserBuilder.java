/**
 * 
 */
package com.keycloak.admin.client.dataacess;

import static com.keycloak.admin.client.config.AuthProperties.LAST_EXPIRYDATE;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.assertj.core.util.Arrays;
import org.keycloak.representations.idm.SocialLinkRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import com.github.javafaker.Faker;
import com.keycloak.admin.client.common.enums.Role;
import com.keycloak.admin.client.common.enums.SocialProvider;
import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.common.utils.DateUtil;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.models.AuthenticationRequest;
import com.keycloak.admin.client.models.EmailStatusUpdateRequest;
import com.keycloak.admin.client.models.ForgotUsernameRequest;
import com.keycloak.admin.client.models.PasswordResetTokenResponse;
import com.keycloak.admin.client.models.PasswordUpdateRequest;
import com.keycloak.admin.client.models.ProfileActivationUpdateRequest;
import com.keycloak.admin.client.models.ResetPasswordFinalRequest;
import com.keycloak.admin.client.models.ResetPasswordRequest;
import com.keycloak.admin.client.models.SearchUserRequest;
import com.keycloak.admin.client.models.StatusUpdateRequest;
import com.keycloak.admin.client.models.UserDetailsUpdateRequest;
import com.keycloak.admin.client.models.UserRegistrationRequest;
import com.keycloak.admin.client.models.UserVO;

import lombok.Data;

/**
 * @author Gbenga
 *
 */
@Data
public class UserBuilder {

	private Faker faker;

	private String id;

	private String username;

	private String email = "john.doe@example.com";

	private String password = "secret";

	private String firstName = "John";

	private String lastName = "Doe";

	private String displayName = "John";

	private String status;

	private List<Role> roles = new ArrayList<>();

	private static final String[] socialProviders = Arrays.array(SocialProvider.GOOGLE.getProviderType(),
			SocialProvider.FACEBOOK.getProviderType(), SocialProvider.GITHUB.getProviderType(),
			SocialProvider.LINKEDIN.getProviderType(), SocialProvider.TWITTER.getProviderType());

	private UserBuilder() {
		faker = new Faker();

		this.id = UUID.randomUUID().toString();
		this.firstName = faker.name().firstName();
		this.lastName = faker.name().lastName();
		this.email = faker.internet().emailAddress();

		this.username = this.email;
		this.password = RandomGenerator.generateRandomPassword();
		this.status = getRandomStatus().toString();

		this.roles.add(Role.ROLE_ADMIN);
	}

	public static UserBuilder user() {
		return new UserBuilder();
	}

	public UserBuilder withId(UUID id) {
		this.id = id == null ? null : id.toString();
		return this;
	}

	public UserBuilder withUsername(String username) {
		this.username = username;
		return this;
	}

	public UserBuilder withEmail(String email) {
		this.email = email;
		return this;
	}

	public UserBuilder withFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	public UserBuilder withLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}

	public UserBuilder withRoles(List<Role> roles) {
		this.roles = roles;
		return this;
	}

	public UserBuilder withPassword(String password) {
		this.password = password;
		return this;
	}

	public UserBuilder withStatus(String statusType) {
		this.status = statusType;
		return this;
	}

	public UserBuilder withDisplayName() {
		this.displayName = displayName(firstName, lastName);
		return this;
	}

	public UserVO userVo() {
		return userVo(null);
	}

	public UserVO userVo(UUID id) {
		Set<String> mySet = this.roles.stream().map(c -> c.getName()).collect(Collectors.toSet());
		SocialLinkRepresentation socialLinkRep = socialLinkRepresentation();

		LocalDateTime creationDate = DateUtil
				.getLocalDateTimeFromLongMillisecs(faker.date().past(30, TimeUnit.DAYS).getTime());

		LocalDateTime lastModifiedDate = DateUtil
				.getLocalDateTimeFromLongMillisecs(faker.date().past(1, TimeUnit.DAYS).getTime());

		return UserVO.builder().id(id != null ? id.toString() : null).username(username).roles(mySet).email(email)
				.firstName(firstName).lastName(lastName).displayName(displayName(firstName, lastName))
				.emailVerified(Boolean.TRUE).disabled(!Boolean.TRUE)
				// Set empty to avoid leakage
				.password(password).createdDate(creationDate)
				.socialProvider(socialLinkRep != null ? socialLinkRep.getSocialProvider() : null)
				.providerUserId(socialLinkRep != null ? socialLinkRep.getSocialUserId() : null)
				.lastModifiedDate(lastModifiedDate)
				// .accountLocked(isAccountLocked)
				// .expired(isAccountExpired)
				// .enableMFA(isMfaEnabled)
				.build();
	}

	public UserRepresentation userRepresentation(UUID id) {
		List<String> myRoleList = this.roles.stream().map(c -> c.getName()).collect(Collectors.toList());

		UserRepresentation representation = new UserRepresentation();
		representation.setId(id.toString());
		representation.setEmail(email);
		representation.setUsername(username);
		representation.setRealmRoles(myRoleList);
		representation.setEmailVerified(false);
		representation.setCreatedTimestamp(System.currentTimeMillis());
		representation.setEnabled(true);
		representation.setLastName(this.lastName);
		representation.setFirstName(this.firstName);

		representation.setSocialLinks(getSocialRepresentationLinks());

		String pastDate = String.valueOf(faker.date().past(120, 90, TimeUnit.DAYS).getTime());
		representation.singleAttribute(LAST_EXPIRYDATE, pastDate);

		representation.setCredentials(null);

		return representation;
	}

	public SearchUserRequest searchUserRepresentation() {
		boolean verifiedEmails = true;

		return SearchUserRequest.builder().email(email).firstName(firstName).lastName(lastName)
				.emailVerified(verifiedEmails).build();
	}

	public UserRegistrationRequest build() {

		return UserRegistrationRequest.builder().firstName(this.firstName).lastName(this.lastName).email(this.email)
				.password(this.password).build();
	}

	public UserDetailsUpdateRequest buildUpdate() {

		return UserDetailsUpdateRequest.builder().firstName(this.firstName).lastName(this.lastName).build();
	}

	public StatusUpdateRequest buildStatusUpdate() {

		return StatusUpdateRequest.builder().status(this.status).build();
	}

	public EmailStatusUpdateRequest buildEmailUpdate() {

		return EmailStatusUpdateRequest.builder().email(this.email).verified(faker.bool().bool()).build();
	}

	public ForgotUsernameRequest buildUsernameRequest() {

		return ForgotUsernameRequest.builder().email(this.email).build();
	}

	public SearchUserRequest buildSearchUserRequest() {

		return SearchUserRequest.builder().firstName(this.firstName)
				.lastName(this.lastName).email(this.email)
				.emailVerified(faker.bool().bool()).build();
	}

	public ResetPasswordRequest buildPasswordResetRequest() {

		return ResetPasswordRequest.builder().email(this.email).build();
	}

	public ResetPasswordFinalRequest buildFinalPasswordResetRequest(String sessionId) {

		return ResetPasswordFinalRequest.builder().username(this.username).newPassword(this.password)
				.sessionId(sessionId).build();
	}

	public PasswordResetTokenResponse buildPasswordResetToken(String sessionId) {

		return PasswordResetTokenResponse.builder().sessionId(sessionId).build();
	}

	public PasswordUpdateRequest buildPasswordUpdateRequest(boolean genPassword, String newPassword) {

		return PasswordUpdateRequest.builder().oldPassword(this.password).newPassword(genPassword ? newPassword : null)
				.build();
	}

	public ProfileActivationUpdateRequest buildProfileActivationRequest() {

		return ProfileActivationUpdateRequest.builder().userId(this.id).enable(faker.bool().bool()).build();
	}

	public AuthenticationRequest loginRequest() {
		return new AuthenticationRequest(this.username, this.password);
	}

	private SocialLinkRepresentation socialLinkRepresentation() {

		SocialLinkRepresentation socialLinkRep = new SocialLinkRepresentation();
		socialLinkRep.setSocialProvider(getRandomSocialProvider());
		socialLinkRep.setSocialUserId(String.valueOf(Faker.instance().number().numberBetween(100, 100000)));
		socialLinkRep.setSocialUsername(this.email);

		return socialLinkRep;
	}

	private List<SocialLinkRepresentation> getSocialRepresentationLinks() {

		List<SocialLinkRepresentation> list = new ArrayList<>();
		list.add(socialLinkRepresentation());

		return list;
	}

	private String getRandomSocialProvider() {
		int num = socialProviders.length;

		int i = ThreadLocalRandom.current().nextInt(0, num);

		return socialProviders[i];
	}

	private String displayName(String firstName, String lastName) {
		String formattedFirstName = StringUtils.isBlank(firstName) ? "" : firstName;
		String formattedLastName = StringUtils.isBlank(lastName) ? "" : lastName;

		return WordUtils.capitalizeFully(formattedFirstName + " " + formattedLastName);
	}

	public List<UserRepresentation> userRepresentationList(int size) {
		List<UserRepresentation> userList = new ArrayList<>();

		for (int i = 0; i < size; ++i) {
			userList.add(userRepresentation(UUID.randomUUID()));
		}

		return userList;
	}
	
	public static List<UserVO> userList() {
		List<UserVO> arrayList = new ArrayList<>();

		arrayList.add(UserBuilder.user().userVo(UUID.randomUUID()));
		arrayList.add(UserBuilder.user().userVo(UUID.randomUUID()));
		arrayList.add(UserBuilder.user().userVo(UUID.randomUUID()));
		arrayList.add(UserBuilder.user().userVo(UUID.randomUUID()));

		return arrayList;
	}

	public static StatusType getRandomStatus() {
		Set<String> setOfStatus = StatusType.getAllTypes();
		int num = setOfStatus.size();

		List<String> arrayList = List.copyOf(setOfStatus);

		int i = ThreadLocalRandom.current().nextInt(0, num - 1);

		return StatusType.fromString(arrayList.get(i));
	}

}
