package iqadot.iqadotkit.config.security.token.impl;

import iqadot.iqadotkit.config.security.token.*;
import lombok.*;
import org.springframework.security.core.*;
import org.springframework.security.core.userdetails.*;


import java.util.*;

@EqualsAndHashCode
@Getter
public class AccessTokenImpl implements AccessToken, UserDetails {
    private final String subject;
    private final Long userId;
    private final Collection<? extends GrantedAuthority> authorities;

    public AccessTokenImpl(String subject, Long userId) {
        this.subject = subject;
        this.userId = userId;
        this.authorities = Collections.emptyList(); // No roles, so use an empty list.
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // You can return null or an empty string for the password.
    }

    @Override
    public String getUsername() {
        return subject;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Account is always considered non-expired.
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Account is always considered non-locked.
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Credentials are always considered non-expired.
    }

    @Override
    public boolean isEnabled() {
        return true; // The user is always considered enabled.
    }

}
