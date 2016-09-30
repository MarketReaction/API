package com.jassoft.markets.api;

import com.jassoft.markets.datamodel.user.User;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Created by jonshaw on 23/09/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringConfiguration.class)
@WebIntegrationTest({
        "spring.data.mongodb.database=" + BaseApiTest.DB_NAME,
        "server.port=0"})
public class UserControllerTest extends BaseApiTest {

    @Value("${local.server.port}")
    private int port;

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testAuthenticate_withInvalidUser() throws Exception {

        Map<String, String> invalidCredentials = new HashMap<>();
        invalidCredentials.put("email", "Invalid@User.com");
        invalidCredentials.put("password", "InvalidPassword");

        given().body(invalidCredentials)
                .contentType(ContentType.JSON)
                .port(port)
                .when().post("/user/authenticate")
                .then().statusCode(400)
                .body("code", equalTo(104)).body("message", equalTo("User does not Exist with Email " + invalidCredentials.get("email")));
    }

    @Test
    public void testAuthenticate_withPasswordUser() throws Exception {

        Map<String, String> invalidCredentials = new HashMap<>();
        invalidCredentials.put("email", "Invalid@User.com");
        invalidCredentials.put("password", "InvalidPassword");

        userRepository.save(new User(null, invalidCredentials.get("email"), "Test", "User", "InvalidPassword", true, null, null, null, null));

        given().body(invalidCredentials)
                .contentType(ContentType.JSON)
                .port(port)
                .when().post("/user/authenticate")
                .then().statusCode(400)
                .body("code", equalTo(105)).body("message", equalTo("Credentials not valid for Email " + invalidCredentials.get("email")));
    }

    @Test
    public void testAuthenticate_withInactiveUser() throws Exception {

        userRepository.deleteAll();

        userRepository.save(new User(null, credentials.get("email"), "Test", "User", encoder.encode(credentials.get("password")), false, null, null, null, null));

        given().body(credentials)
                .contentType(ContentType.JSON)
                .port(port)
                .when().post("/user/authenticate")
                .then().statusCode(400)
                .body("code", equalTo(106)).body("message", equalTo("User not activated with Email " + credentials.get("email")));
    }

    @Test
    public void testAuthenticate_withValidUser() throws Exception {

        Response response = given().body(credentials)
                .contentType(ContentType.JSON)
                .port(port)
                .when().post("/user/authenticate").andReturn();

        System.out.println("Response: " + response.asString());

        response.then().statusCode(200)
                .body("forename", equalTo("Test"))
                .body("surname", equalTo("User"))
                .body("activated", equalTo(true))
                .body("password", equalTo(null))
                .body("activationId", equalTo(null)).extract().asString();
    }

    @Test
    public void testAuthenticate_withValidAdminUser() throws Exception {

        Response response = given().body(adminCredentials)
                .contentType(ContentType.JSON)
                .port(port)
                .when().post("/user/authenticate").andReturn();

        System.out.println("Response: " + response.asString());

        response.then().statusCode(200)
                .body("forename", equalTo("Admin"))
                .body("surname", equalTo("User"))
                .body("activated", equalTo(true))
                .body("password", equalTo(null))
                .body("activationId", equalTo(null)).extract().asString();
    }

    @Test
    public void testGetCurrentUser() throws Exception {
        testAuthenticate_withValidUser();

        User user = userRepository.findByEmail(credentials.get("email"));

        Response response = given().header(HEADER_SECURITY_TOKEN, user.getToken())
                .header(HEADER_SECURITY_EMAIL, user.getEmail())
                .contentType(ContentType.JSON)
                .port(port)
                .when().get("/user/").andReturn();

        System.out.println("Response: " + response.asString());

        response.then().statusCode(200)
                .body("forename", equalTo("Test"))
                .body("surname", equalTo("User"))
                .body("activated", equalTo(true))
                .body("password", equalTo(null))
                .body("activationId", equalTo(null));
    }

    @Test
    public void testGetUser_asNonAdmin() throws Exception {
        testAuthenticate_withValidUser();

        User user = userRepository.findByEmail(credentials.get("email"));

        Response response = given().header(HEADER_SECURITY_TOKEN, user.getToken())
                .header(HEADER_SECURITY_EMAIL, user.getEmail())
                .contentType(ContentType.JSON)
                .port(port)
                .when().get("/user/" + user.getId()).andReturn();

        System.out.println("Response: " + response.asString());

        response.then().statusCode(403);
    }

    @Test
    public void testGetUser_asAdmin() throws Exception {
        testAuthenticate_withValidAdminUser();

        User user = userRepository.findByEmail(adminCredentials.get("email"));

        Response response = given().header(HEADER_SECURITY_TOKEN, user.getToken())
                .header(HEADER_SECURITY_EMAIL, user.getEmail())
                .contentType(ContentType.JSON)
                .port(port)
                .when().get("/user/" + user.getId()).andReturn();

        System.out.println("Response: " + response.asString());

        response.then().statusCode(200)
                .body("forename", equalTo("Admin"))
                .body("surname", equalTo("User"))
                .body("activated", equalTo(true))
                .body("password", equalTo(null))
                .body("activationId", equalTo(null)).extract().asString();
    }

    @Test
    public void testGetUsers() throws Exception {
        testAuthenticate_withValidAdminUser();

        User user = userRepository.findByEmail(adminCredentials.get("email"));

        Response response = given().header(HEADER_SECURITY_TOKEN, user.getToken())
                .header(HEADER_SECURITY_EMAIL, user.getEmail())
                .contentType(ContentType.JSON)
                .port(port)
                .when().get("/user/list").andReturn();

        System.out.println("Response: " + response.asString());

        response.then().statusCode(200);
    }

    @Test
    public void testTimeline() throws Exception {

    }

    @Test
    public void testRegister() throws Exception {

    }

    @Test
    public void testConfirm() throws Exception {

    }

    @Test
    public void testEnableUser() throws Exception {

    }

    @Test
    public void testDisableUser() throws Exception {

    }

    @Test
    public void testDeleteUser() throws Exception {

    }

    @Test
    public void testHandleException() throws Exception {

    }

    @Test
    public void testManageOptions() throws Exception {

    }

    @Test
    @Ignore
    public void testOAuth2Google() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjRiNDQ5ZTRiZGRkNjgyNzEyYTkxMzk3ZGZkMjVlMzg2MzJjNjVjMTAifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwiYXRfaGFzaCI6Il9RcDRDeDZDaGp1aGNsa0kwM05xbnciLCJhdWQiOiIxMDE5NTMxNTY4NjIyLTJxM2w0OGxlYmRubTNobjE1dmlzNWg0Mmlsb2Q0cjM4LmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwic3ViIjoiMTAxNzc1MTI3MDQxODE2OTk1Mzk4IiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImF6cCI6IjEwMTk1MzE1Njg2MjItMnEzbDQ4bGViZG5tM2huMTV2aXM1aDQyaWxvZDRyMzguYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJoZCI6Imphc3NvZnQuY28udWsiLCJlbWFpbCI6ImpvbkBqYXNzb2Z0LmNvLnVrIiwiaWF0IjoxNDcxMzUyNTA0LCJleHAiOjE0NzEzNTYxMDQsIm5hbWUiOiJKb25hdGhhbiBTaGF3IiwicGljdHVyZSI6Imh0dHBzOi8vbGgzLmdvb2dsZXVzZXJjb250ZW50LmNvbS8tWm55THVFYVdnRzgvQUFBQUFBQUFBQUkvQUFBQUFBQUFITTQvX0tBRENaN0l6RGcvczk2LWMvcGhvdG8uanBnIiwiZ2l2ZW5fbmFtZSI6IkpvbmF0aGFuIiwiZmFtaWx5X25hbWUiOiJTaGF3IiwibG9jYWxlIjoiZW4tR0IifQ.MFD90RfsG9-3qgOD_sjAlG6dDfy5ZaP8IT9zWSUXDONZJVgRA2OugLLZGjpFHA6ltAwKigXrhCaKm3iEROwbweMGrDJ694--9Rcwo-LTZ8eQi_PhDkAvbavlAPIuM3xGkFeMj30mzPYG3si-bhfdzfPJwYXqPcCArZA6UxLmlklHLFUM-_RJ3PfMz3xrngkNFKvf-v-_cX6vEaASMMWKuwatOv62QtGdVshUqZpzOfEEMC5M3bS3HKouiPPCgaYsF7-Hohjqa_Fbt2Qg_SiCHIWohCIiu8DFaD9nufkmqac4nIza1EczXL75Xi1g4Mv_bPIc9R7g_4h3jo0AlDWaLA";

        Response response = given().body(token)
                .contentType(ContentType.JSON)
                .port(port)
                .when().post("/user/oauth2/google").andReturn();

        System.out.println("Response: " + response.asString());

        response.then().statusCode(200)
                .body("forename", equalTo("Jonathan"))
                .body("surname", equalTo("Shaw"))
                .body("activated", equalTo(true))
                .body("password", equalTo(null))
                .body("activationId", equalTo(null)).extract().asString();
    }
}