package com.jassoft.markets.api;

import com.jassoft.markets.datamodel.company.Exchange;
import com.jassoft.markets.repository.ExchangeRepository;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Created by jonshaw on 25/09/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringConfiguration.class)
@WebIntegrationTest({
        "spring.data.mongodb.database=" + BaseApiTest.DB_NAME,
        "server.port=0"})
public class ExchangeControllerTest extends BaseApiTest {

    @Autowired
    private ExchangeRepository exchangeRepository;

    @Value("${local.server.port}")
    private int port;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        exchangeRepository.deleteAll();
    }

    @Test
    public void testGetExchange() throws Exception {

        String exchangeId = UUID.randomUUID().toString();
        exchangeRepository.save(new Exchange(exchangeId, "LON", "London Stock Exchange", true, true, "GMT", "GBP", "UK"));

        Response response = given().header(HEADER_SECURITY_TOKEN, adminToken)
                .header(HEADER_SECURITY_EMAIL, adminCredentials.get("email"))
                .contentType(ContentType.JSON)
                .port(port)
                .when().get("/exchange/" + exchangeId).andReturn();

                System.out.println("Response: " + response.asString());

        response.then().statusCode(200)
                .body("code", equalTo("LON"))
                .body("name", equalTo("London Stock Exchange"))
                .body("intraday", equalTo(true))
                .body("enabled", equalTo(true));

    }

    @Test
    public void testGetExchanges() throws Exception {
        exchangeRepository.save(new Exchange(UUID.randomUUID().toString(), "LON", "London Stock Exchange", true, true, "GMT", "GBP", "UK"));
        exchangeRepository.save(new Exchange(UUID.randomUUID().toString(), "NYSE", "New York Stock Exchange", true, true, "GMT", "USD", "US"));

        Response response = given().header(HEADER_SECURITY_TOKEN, adminToken)
                .header(HEADER_SECURITY_EMAIL, adminCredentials.get("email"))
                .contentType(ContentType.JSON)
                .port(port)
                .when().get("/exchange").andReturn();

        System.out.println("Response: " + response.asString());

        response.then().statusCode(200)
                .body("code[0]", equalTo("LON"))
                .body("name[0]", equalTo("London Stock Exchange"))
                .body("intraday[0]", equalTo(true))
                .body("enabled[0]", equalTo(true))
                .body("code[1]", equalTo("NYSE"))
                .body("name[1]", equalTo("New York Stock Exchange"))
                .body("intraday[1]", equalTo(true))
                .body("enabled[1]", equalTo(true));
    }

    @Test
    public void testGetAllExchanges() throws Exception {
        exchangeRepository.save(new Exchange(UUID.randomUUID().toString(), "LON", "London Stock Exchange", true, false, "GMT", "GBP", "UK"));
        exchangeRepository.save(new Exchange(UUID.randomUUID().toString(), "NYSE", "New York Stock Exchange", false, true, "GMT", "USD", "US"));

        Response response = given().header(HEADER_SECURITY_TOKEN, adminToken)
                .header(HEADER_SECURITY_EMAIL, adminCredentials.get("email"))
                .contentType(ContentType.JSON)
                .port(port)
                .when().get("/exchange/all").andReturn();

        System.out.println("Response: " + response.asString());

        response.then().statusCode(200)
                .body("code[0]", equalTo("LON"))
                .body("name[0]", equalTo("London Stock Exchange"))
                .body("intraday[0]", equalTo(false))
                .body("enabled[0]", equalTo(true))
                .body("code[1]", equalTo("NYSE"))
                .body("name[1]", equalTo("New York Stock Exchange"))
                .body("intraday[1]", equalTo(true))
                .body("enabled[1]", equalTo(false));
    }

    @Test
    public void testEnableExchange() throws Exception {
        String exchangeId = UUID.randomUUID().toString();
        exchangeRepository.save(new Exchange(exchangeId, "LON", "London Stock Exchange", true, true, "GMT", "GBP", "UK"));

        Response response = given().header(HEADER_SECURITY_TOKEN, adminToken)
                .header(HEADER_SECURITY_EMAIL, adminCredentials.get("email"))
                .contentType(ContentType.JSON)
                .port(port)
                .when().get("/exchange/" + exchangeId + "/enable").andReturn();

        System.out.println("Response: " + response.asString());

        response.then().statusCode(200)
                .body("code", equalTo("LON"))
                .body("name", equalTo("London Stock Exchange"))
                .body("intraday", equalTo(true))
                .body("enabled", equalTo(true));
    }

    @Test
    public void testDisableExchange() throws Exception {
        String exchangeId = UUID.randomUUID().toString();
        exchangeRepository.save(new Exchange(exchangeId, "LON", "London Stock Exchange", true, true, "GMT", "GBP", "UK"));

        Response response = given().header(HEADER_SECURITY_TOKEN, adminToken)
                .header(HEADER_SECURITY_EMAIL, adminCredentials.get("email"))
                .contentType(ContentType.JSON)
                .port(port)
                .when().get("/exchange/" + exchangeId + "/disable").andReturn();

        System.out.println("Response: " + response.asString());

        response.then().statusCode(200)
                .body("code", equalTo("LON"))
                .body("name", equalTo("London Stock Exchange"))
                .body("intraday", equalTo(true))
                .body("enabled", equalTo(false));
    }

    @Test
    public void testManageOptions() throws Exception {

    }
}