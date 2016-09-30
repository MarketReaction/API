package com.jassoft.markets.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by jonshaw on 23/09/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringConfiguration.class)
@WebIntegrationTest({
        "spring.data.mongodb.database=" + BaseApiTest.DB_NAME,
        "server.port=0"})
public class CompanyControllerTest extends BaseApiTest {

    private final String HEADER_SECURITY_TOKEN = "X-AuthToken";

    @Test
    public void testGetCompany() throws Exception {
//        given().header(HEADER_SECURITY_TOKEN, "rf3egefe").when().get("/company/i7yrg3eiuf").then().statusCode(200);

    }

    @Test
    public void testGetCompanysByExchange() throws Exception {

    }

    @Test
    public void testGetCompanysByIds() throws Exception {

    }

    @Test
    public void testGetCompanysByExchangePageable() throws Exception {

    }

    @Test
    public void testSearchCompanysByExchangePageable() throws Exception {

    }

    @Test
    public void testSearchByName() throws Exception {

    }

    @Test
    public void testFollowCompany() throws Exception {

    }

    @Test
    public void testUnfollowCompany() throws Exception {

    }

    @Test
    public void testManageOptions() throws Exception {

    }
}