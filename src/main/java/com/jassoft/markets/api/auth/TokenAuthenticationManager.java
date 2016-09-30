package com.jassoft.markets.api.auth;

import com.jassoft.markets.datamodel.user.User;
import com.jassoft.markets.repository.UserRepository;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Created by Jonny on 14/08/2014.
 */
//@Component
public class TokenAuthenticationManager implements AuthenticationManager
{
    private UserRepository userRepository;

    public TokenAuthenticationManager(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        ApiAuthenticationToken apiAuthenticationToken = (ApiAuthenticationToken) authentication;

        if(apiAuthenticationToken.getToken()==null)
            return null;

        User user = userRepository.findByEmailAndToken(apiAuthenticationToken.getEmail(), apiAuthenticationToken.getToken());

        if(user != null) {
            apiAuthenticationToken.setUser(user);

            if(user.getoAuth2Provider() == null) {
                user.setTokenExpiry(new DateTime(DateTimeZone.UTC).plusHours(24).toDate());
                userRepository.save(user);
            }
        }

        return apiAuthenticationToken;
    }

}