package uk.co.jassoft.markets.api;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.apache.commons.lang.time.DateUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.co.jassoft.markets.datamodel.company.Company;
import uk.co.jassoft.markets.datamodel.company.sentiment.EntitySentiment;
import uk.co.jassoft.markets.datamodel.company.sentiment.PeriodType;
import uk.co.jassoft.markets.datamodel.company.sentiment.StorySentiment;
import uk.co.jassoft.markets.datamodel.story.NamedEntities;
import uk.co.jassoft.markets.repository.CompanyRepository;
import uk.co.jassoft.markets.repository.StorySentimentRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Created by jonshaw on 29/09/15.
 */
public class SentimentControllerTest  extends BaseApiTest {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private StorySentimentRepository storySentimentRepository;

    @Value("${local.server.port}")
    private int port;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        companyRepository.deleteAll();
    }

    private void generateStorySentiment(String company, Date date, Integer sentiment) {
        ArrayList<EntitySentiment> entitySentiments = new ArrayList<>();
        entitySentiments.add(new EntitySentiment("Name", sentiment));

        storySentimentRepository.save(new StorySentiment(company, date, "SampleStoryId", entitySentiments));
    }

    @Test
    public void testGetSentimentsByCompany() throws Exception {

        String companyId = UUID.randomUUID().toString();

        generateStorySentiment(companyId, new DateTime().minusDays(6).toDate(), 100);
        generateStorySentiment(companyId, new DateTime().minusDays(5).toDate(), 95);
        generateStorySentiment(companyId, new DateTime().minusDays(5).toDate(), 35);
        generateStorySentiment(companyId, new DateTime().minusDays(4).toDate(), 90);
        generateStorySentiment(companyId, new DateTime().minusDays(4).toDate(), 20);
        generateStorySentiment(companyId, new DateTime().minusDays(3).toDate(), 85);
        generateStorySentiment(companyId, new DateTime().minusDays(3).toDate(), 75);
        generateStorySentiment(companyId, new DateTime().minusDays(3).toDate(), 65);
        generateStorySentiment(companyId, new DateTime().minusDays(3).toDate(), 55);
        generateStorySentiment(companyId, new DateTime().minusDays(2).toDate(), 95);
        generateStorySentiment(companyId, new DateTime().minusDays(1).toDate(), 85);

        Company company = new Company(companyId, "TEST", "TestCompany", "Testing", "Information about testing", new NamedEntities());

        companyRepository.save(company);

        Response response = given().header(HEADER_SECURITY_TOKEN, adminToken)
                .header(HEADER_SECURITY_EMAIL, adminCredentials.get("email"))
                .contentType(ContentType.JSON)
                .port(port)
                .when().get("/sentiment/company/" + companyId + "/period/" + PeriodType.Day).andReturn();

        System.out.println("Response: " + response.asString());

        response.then().statusCode(200)
                .body("date[0]", equalTo(DateUtils.truncate(new DateTime().minusDays(6).toDate(), Calendar.DATE).getTime()))
                .body("sentiment[0]", equalTo(100))
                .body("date[1]", equalTo(DateUtils.truncate(new DateTime().minusDays(5).toDate(), Calendar.DATE).getTime()))
                .body("sentiment[1]", equalTo(130))
                .body("date[2]", equalTo(DateUtils.truncate(new DateTime().minusDays(4).toDate(), Calendar.DATE).getTime()))
                .body("sentiment[2]", equalTo(110))
                .body("date[3]", equalTo(DateUtils.truncate(new DateTime().minusDays(3).toDate(), Calendar.DATE).getTime()))
                .body("sentiment[3]", equalTo(280))
                .body("date[4]", equalTo(DateUtils.truncate(new DateTime().minusDays(2).toDate(), Calendar.DATE).getTime()))
                .body("sentiment[4]", equalTo(95))
                .body("date[5]", equalTo(DateUtils.truncate(new DateTime().minusDays(1).toDate(), Calendar.DATE).getTime()))
                .body("sentiment[5]", equalTo((85)));

    }

    @Test
    public void testGetChartToday() throws Exception {

        String companyId1 = UUID.randomUUID().toString();
        generateStorySentiment(companyId1, new DateTime().toDate(), 200);
        Company company1 = new Company(companyId1, "TEST", "TestCompany1", "Testing", "Information about testing", new NamedEntities());

        String companyId2 = UUID.randomUUID().toString();
        generateStorySentiment(companyId2, new DateTime().toDate(), 85);
        Company company2 = new Company(companyId2, "TEST", "TestCompany2", "Testing", "Information about testing", new NamedEntities());

        companyRepository.save(company1);
        companyRepository.save(company2);

        Response response = given().header(HEADER_SECURITY_TOKEN, adminToken)
                .header(HEADER_SECURITY_EMAIL, adminCredentials.get("email"))
                .contentType(ContentType.JSON)
                .port(port)
                .when().get("/sentiment/highest/period/" + PeriodType.Day + "/limit/1").andReturn();

        System.out.println("Response: " + response.asString());

        response.then().statusCode(200)
                .body("companyId[0]", equalTo(companyId1))
                .body("companyName[0]", equalTo("TestCompany1"))
                .body("sentiment[0]", equalTo(200));

        Response response2 = given().header(HEADER_SECURITY_TOKEN, adminToken)
                .header(HEADER_SECURITY_EMAIL, adminCredentials.get("email"))
                .contentType(ContentType.JSON)
                .port(port)
                .when().get("/sentiment/lowest/period/" + PeriodType.Day + "/limit/1").andReturn();

        System.out.println("Response: " + response2.asString());

        response2.then().statusCode(200)
                .body("companyId[0]", equalTo(companyId2))
                .body("companyName[0]", equalTo("TestCompany2"))
                .body("sentiment[0]", equalTo(85));
    }

    @Test
    public void testManageOptions() throws Exception {

    }
}