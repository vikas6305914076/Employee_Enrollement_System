package com.company.ems.security;

import com.company.ems.employee.entity.EmployeeStatus;
import com.company.ems.employee.entity.UserRole;
import java.util.Collection;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@EqualsAndHashCode(of = "username")
public class AuthenticatedUser implements UserDetails {

    private final Long employeeId;
    private final String username;
    private final String password;
    private final UserRole userRole;
    private final EmployeeStatus status;
    private final List<GrantedAuthority> authorities;

    public AuthenticatedUser(
            Long employeeId,
            String username,
            String password,
            UserRole userRole,
            EmployeeStatus status
    ) {
        this.employeeId = employeeId;
        this.username = username;
        this.password = password;
        this.userRole = userRole;
        this.status = status;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + userRole.name()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return EmployeeStatus.ACTIVE.equals(status);
    }
}
