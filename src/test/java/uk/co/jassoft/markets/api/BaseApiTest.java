package uk.co.jassoft.markets.api;

import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.co.jassoft.markets.datamodel.user.User;
import uk.co.jassoft.markets.repository.UserRepository;
import uk.co.jassoft.utils.BaseTest;
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
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringConfiguration.class)
@WebIntegrationTest({
        "spring.data.mongodb.database=" + BaseApiTest.DB_NAME,
        "server.port=0",
        "OAUTH_GOOGLE_TOKEN=Test"})
public abstract class BaseApiTest extends BaseTest {

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
