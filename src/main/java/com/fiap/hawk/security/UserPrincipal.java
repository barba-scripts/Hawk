package com.fiap.hawk.security;

import com.fiap.hawk.domain.Role;
import com.fiap.hawk.domain.UserDocument;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

	private final Integer publicId;
	private final String email;
	private final String passwordHash;
	private final String name;
	private final Role role;
	private final String division;
	private final boolean active;

	public UserPrincipal(Integer publicId, String email, String passwordHash, String name,
			Role role, String division, boolean active) {
		this.publicId = publicId;
		this.email = email;
		this.passwordHash = passwordHash;
		this.name = name;
		this.role = role;
		this.division = division;
		this.active = active;
	}

	public static UserPrincipal from(UserDocument user) {
		return new UserPrincipal(
				user.getPublicId(),
				user.getEmail(),
				user.getPasswordHash(),
				user.getName(),
				user.getRole(),
				user.getDivision(),
				user.isActive()
		);
	}

	public Integer getPublicId() {
		return publicId;
	}

	public String getName() {
		return name;
	}

	public Role getRole() {
		return role;
	}

	public String getDivision() {
		return division;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(role.authority()));
	}

	@Override
	public String getPassword() {
		return passwordHash;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return active;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return active;
	}
}
