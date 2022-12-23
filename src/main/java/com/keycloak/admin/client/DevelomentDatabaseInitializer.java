/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.keycloak.admin.client;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.keycloak.representations.idm.SocialLinkRepresentation;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;
import com.keycloak.admin.client.common.enums.Role;
import com.keycloak.admin.client.common.enums.SocialProvider;
import com.keycloak.admin.client.common.utils.DateUtil;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.models.UserVO;

/**
 * @author Gbenga
 */
// tag::code[]
@Component
@Profile(SpringProfiles.DEV)
public class DevelomentDatabaseInitializer {

	@Bean
	CommandLineRunner initialize(MongoOperations mongo) {
		return args -> {
			mongo.save(userVo());
			mongo.save(userVo());
		};
	}
	
	private static final List<String> socialProviders = Arrays.asList(SocialProvider.GOOGLE.getProviderType(),
			SocialProvider.FACEBOOK.getProviderType(), SocialProvider.GITHUB.getProviderType(),
			SocialProvider.LINKEDIN.getProviderType(), SocialProvider.TWITTER.getProviderType());
	
	public UserVO userVo() {
		Faker faker = Faker.instance();
		
		UUID id = UUID.randomUUID();
		String firstName = faker.name().firstName();
		String lastName = faker.name().lastName();
		String email = faker.internet().emailAddress();

		String username = email;
		String password = RandomGenerator.generateRandomPassword();
		//String status = getRandomStatus().toString();
		
		Set<Role> setOfRoles = new HashSet<>();   
		setOfRoles.add(getRandomRole());
		Set<String> mySet = setOfRoles.stream().map(c -> c.getName()).collect(Collectors.toSet());
		
		SocialLinkRepresentation socialLinkRep = new SocialLinkRepresentation();
		socialLinkRep.setSocialProvider(getRandomSocialProvider());
		socialLinkRep.setSocialUserId(String.valueOf(Faker.instance().number().numberBetween(100, 100000)));
		socialLinkRep.setSocialUsername(email);

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
	
	private String getRandomSocialProvider() {
		int num = socialProviders.size();

		int i = ThreadLocalRandom.current().nextInt(0, num);

		return socialProviders.get(i); 
	}
	
	private static Role getRandomRole() {
		Set<String> setOfRoles = Role.getAllTypes();
		int num = setOfRoles.size();

		List<String> arrayList = List.copyOf(setOfRoles);

		int i = ThreadLocalRandom.current().nextInt(0, num - 1);

		return Role.fromString(arrayList.get(i));
	}
	
	private String displayName(String firstName, String lastName) {
		String formattedFirstName = StringUtils.isBlank(firstName) ? "" : firstName;
		String formattedLastName = StringUtils.isBlank(lastName) ? "" : lastName;

		return WordUtils.capitalizeFully(formattedFirstName + " " + formattedLastName);
	}

}
// end::code[]

