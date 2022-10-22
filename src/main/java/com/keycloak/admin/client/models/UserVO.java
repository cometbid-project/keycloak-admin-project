/**
 *
 */
package com.keycloak.admin.client.models;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.keycloak.admin.client.validators.qualifiers.ValidEmail;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

/**
 * @author Gbenga
 *
 */
@Data
@Schema(name = "User", description = "User respresentation")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserVO  implements UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2672877971238872267L;

	private String id;

	@Schema(name = "username", description = "username (email)", required = true, example = "john_doe@yahoo.com")
	@JsonProperty("username")
	@Size(max = 330, message = "{User.username.size}")
	protected String username;

	@Schema(name = "email", description = "user's unique email", required = true, example = "john_doe@yahoo.com")
	@JsonProperty("email")
	@ValidEmail(message = "{User.email.invalid}")
	@Size(max = 330, message = "{User.email.size}")
	protected String email;
	
	@Schema(name = "first name", description = "first name on profiles", required = true, example = "John")	
	@JsonProperty("first_name")
	@NotBlank(message = "{FirstName.notBlank}")
	@Size(min = 1, max = 200, message = "{FirstName.size}")
	private String firstName;

	@Schema(name = "Last name", description = "last name on profiles", required = true, example = "Doe")
	@JsonProperty("last_name")
	@NotBlank(message = "{LastName.notBlank}")
	@Size(min = 1, max = 200, message = "{LastName.size}")
	private String lastName;

	@Schema(name = "displayName", description = "user's display name", required = true, example = "Martins Origi")
	@Size(max = 40, message = "{User.displayName.size}")
	@JsonProperty("display_name")
	protected String displayName;

	@JsonProperty("creation_date")
	protected LocalDateTime createdDate;

	@JsonProperty("last_modified_date")
	protected LocalDateTime lastModifiedDate;

	@JsonIgnore
	private String providerUserId;

	@JsonProperty("social_provider")
	private String socialProvider;

	@JsonProperty("2fa_enabled")
	private boolean enable2FA; //

	@JsonIgnore
	private String password;

	@NotEmpty
	@Size(min = 1, max = 1, message = "{User.roles.size}")
	private Set<@NotBlank String> roles;

	private boolean accountLocked; //

	private boolean disabled;

	private boolean expired;

	private boolean emailVerified;

	
	public boolean addRole(String role) {
		if (roles == null) {
			roles = new HashSet<>();
		}
		return roles.add(role);
	}
	
	@JsonIgnore
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.roles.stream().map(authority -> new SimpleGrantedAuthority(authority)).collect(Collectors.toList());
	}
	
	public String getDisplayName() {
		return displayName(this.firstName, this.lastName);
	}
	
	@Override
	@JsonIgnore
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return !this.expired;
	}

	@Override
	@JsonIgnore
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return !this.accountLocked;
	}

	@Override
	@JsonIgnore
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return !this.emailVerified;
	}

	@Override
	@JsonIgnore
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return !this.disabled;
	}

	@Override
	@JsonIgnore
	public String getPassword() {
		return "";
	}
	
	public static String displayName(String firstName, String lastName) {
		String formattedFirstName = StringUtils.isBlank(firstName) ? "": firstName;
		String formattedLastName = StringUtils.isBlank(lastName) ? "": lastName;
		
		return WordUtils.capitalizeFully(formattedFirstName + " " + formattedLastName);
	}

}
