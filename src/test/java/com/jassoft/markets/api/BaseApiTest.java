package com.jassoft.markets.api;

import com.jassoft.markets.datamodel.user.User;
import com.jassoft.markets.repository.UserRepository;
import com.jassoft.utils.BaseTest;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

/**
 * Created by jonshaw on 19/08/15.
 */
public class BaseApiTest extends BaseTest {

    protected final String HEADER_SECURITY_TOKEN = "X-AuthToken";
    protected final String HEADER_SECURITY_EMAIL = "X-AuthEmail";

    protected final PasswordEncoder encoder = new BCryptPasswordEncoder();

    protected Map<String, String> credentials = new HashMap<>();
    protected Map<String, String> adminCredentials = new HashMap<>();

    protected String userToken = UUID.randomUUID().toString();
    protected String adminToken = UUID.randomUUID().toString();

    @Autowired
    protected UserRepository userRepository;

    @Before
    public void setUp() throws Exception {
        userRepository.deleteAll();

        credentials.put("email", "Test@User.com");
        credentials.put("password", "TestPassword");

        adminCredentials.put("email", "Admin@User.com");
        adminCredentials.put("password", "AdminPassword");

        User user = new User(null, credentials.get("email"), "Test", "User", encoder.encode(credentials.get("password")), true, null, null, null, null);
        user.setToken(userToken);
        user.setTokenExpiry(new DateTime(DateTimeZone.UTC).plusHours(1).toDate());
        user.setLastLogin(new Date());

        userRepository.save(user);

        Collection<String> roles = new ArrayList<>();
        roles.add("ROLE_ADMIN");

        User adminUser = new User(null, adminCredentials.get("email"), "Admin", "User", encoder.encode(adminCredentials.get("password")), true, null, roles, null, null);
        adminUser.setToken(adminToken);
        adminUser.setTokenExpiry(new DateTime(DateTimeZone.UTC).plusHours(1).toDate());
        adminUser.setLastLogin(new Date());

        userRepository.save(adminUser);

    }


}