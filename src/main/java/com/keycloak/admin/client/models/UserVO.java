/**
 *
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.keycloak.admin.client.common.utils.LocalDateTimeAdapter;
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
@Value
@Schema(name = "User", description = "User respresentation")
@Builder
//@AllArgsConstructor
//@NoArgsConstructor
@XmlRootElement(name = "user")
@XmlType(name = "user")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(Include.ALWAYS)
//@JsonIgnoreProperties(ignoreUnknown = true)
public class UserVO implements UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2672877971238872267L;

	private String id;

	@Schema(name = "username", description = "username (email)", required = true, example = "john_doe@yahoo.com")
	@JsonProperty("username")
	@Size(max = 50, message = "{User.username.size}")
	protected String username;

	@Schema(name = "email", description = "user's unique email", required = true, example = "john_doe@yahoo.com")
	@JsonProperty("email")
	@ValidEmail
	@Size(max = 50, message = "{User.email.size}")
	protected String email;

	@Schema(name = "first name", description = "first name on profiles", required = true, example = "John")
	@JsonProperty("first_name")
	@NotBlank(message = "{FirstName.notBlank}")
	@Size(min = 1, max = 50, message = "{FirstName.size}")
	private String firstName;

	@Schema(name = "Last name", description = "last name on profiles", required = true, example = "Doe")
	@JsonProperty("last_name")
	@NotBlank(message = "{LastName.notBlank}")
	@Size(min = 1, max = 50, message = "{LastName.size}")
	private String lastName;

	@Schema(name = "displayName", description = "user's display name", required = true, example = "Martins Origi")
	// @Size(max = 101, message = "{User.displayName.size}")
	@JsonProperty("display_name")
	protected String displayName;

	@XmlJavaTypeAdapter(value = LocalDateTimeAdapter.class)
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonProperty("creation_date")
	protected LocalDateTime createdDate;

	@XmlJavaTypeAdapter(value = LocalDateTimeAdapter.class)
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonProperty("last_modified_date")
	protected LocalDateTime lastModifiedDate;

	@JsonIgnore
	private String providerUserId;

	@JsonProperty("social_provider")
	private String socialProvider;

	@JsonProperty("mfa_enabled")
	private boolean enableMFA; //

	@JsonIgnore
	private String password;

	@Builder.Default
	@NotEmpty(message = "{User.roles.empty}")
	@Size(min = 1, max = 1, message = "{User.roles.size}")
	private Set<@NotBlank String> roles = new HashSet<>();

	private boolean accountLocked; //

	private boolean disabled;

	private boolean expired;

	private boolean emailVerified;

	public boolean addRole(String role) {
		return roles.add(role);
	}

	@JsonIgnore
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return sortAuthorities(
				this.roles.stream().map(authority -> 
					new SimpleGrantedAuthority(authority)
				)
				.collect(Collectors.toSet())
		);
	}

	private static SortedSet<GrantedAuthority> sortAuthorities(Collection<? extends GrantedAuthority> authorities) {
		Assert.notNull(authorities, "Cannot pass a null GrantedAuthority collection");
		// Ensure array iteration order is predictable (as per
		// UserDetails.getAuthorities() contract and SEC-717)
		SortedSet<GrantedAuthority> sortedAuthorities = new TreeSet<>(new AuthorityComparator());
		for (GrantedAuthority grantedAuthority : authorities) {
			Assert.notNull(grantedAuthority, "GrantedAuthority list cannot contain any null elements");
			sortedAuthorities.add(grantedAuthority);
		}
		return sortedAuthorities;
	}
	
	private static class AuthorityComparator implements Comparator<GrantedAuthority>, Serializable {

		private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

		@Override
		public int compare(GrantedAuthority g1, GrantedAuthority g2) {
			// Neither should ever be null as each entry is checked before adding it to
			// the set. If the authority is null, it is a custom authority and should
			// precede others.
			if (g2.getAuthority() == null) {
				return -1;
			}
			if (g1.getAuthority() == null) {
				return 1;
			}
			return g1.getAuthority().compareTo(g2.getAuthority());
		}
	}

	
	public String getDisplayName() {
		return displayName(this.firstName, this.lastName);
	}
	
	@JsonProperty("roles")
	public List<String> getRoles() {
		return new ArrayList<>(roles);
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
		return null;
	}

	public static String displayName(String firstName, String lastName) {
		String formattedFirstName = StringUtils.isBlank(firstName) ? "" : firstName;
		String formattedLastName = StringUtils.isBlank(lastName) ? "" : lastName;

		return WordUtils.capitalizeFully(formattedFirstName + " " + formattedLastName);
	}

	/**
	 * @param id
	 * @param username
	 * @param email
	 * @param firstName
	 * @param lastName
	 * @param displayName
	 * @param createdDate
	 * @param lastModifiedDate
	 * @param providerUserId
	 * @param socialProvider
	 * @param enableMFA
	 * @param password
	 * @param roles
	 * @param accountLocked
	 * @param disabled
	 * @param expired
	 * @param emailVerified
	 */
	@JsonCreator
	public UserVO(String id, String username, String email, String firstName, String lastName, String displayName,
			LocalDateTime createdDate, LocalDateTime lastModifiedDate, String providerUserId, String socialProvider,
			boolean enableMFA, String password, Set<@NotBlank String> roles, boolean accountLocked, boolean disabled,
			boolean expired, boolean emailVerified) {
		super();
		this.id = id;
		this.username = username;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.displayName = displayName;
		this.createdDate = createdDate;
		this.lastModifiedDate = lastModifiedDate;
		this.providerUserId = providerUserId;
		this.socialProvider = socialProvider;
		this.enableMFA = enableMFA;
		this.password = null;
		this.roles = roles;
		this.accountLocked = accountLocked;
		this.disabled = disabled;
		this.expired = expired;
		this.emailVerified = emailVerified;
	}

	/**
	 * 
	 */
	private UserVO() {
		this(null, null, null, null, null, null, null, null, null, null, false, null, new HashSet<>(), false, false,
				false, false);
	}

}
