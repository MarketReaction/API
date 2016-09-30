package com.jassoft.markets.api.auth;

import com.jassoft.markets.datamodel.user.User;
import org.joda.time.DateTime;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Jonny on 14/08/2014.
 */
public class ApiAuthenticationToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = 1L;
    private final String email;
    private final String token;
    private User principal;

    private Collection<GrantedAuthority> grantedAuthorities;

    public ApiAuthenticationToken(String email, String token) {
        super(null);
        this.email = email;
        this.token = token;
        grantedAuthorities = new ArrayList<>();
    }

    public void setUser(User user) {
        this.principal = user;

        if(this.principal != null && new DateTime(this.principal.getTokenExpiry()).isAfterNow()) {
            super.setAuthenticated(true);
        }
        else
        {
            super.setAuthenticated(false);
            return;
        }

        for (String role : this.principal.getRoles())
            grantedAuthorities.add(new SimpleGrantedAuthority(role));

    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities()
    {
        return grantedAuthorities;
    }
}