package uk.co.jassoft.markets.api;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.ExtractableResponse;
import com.jayway.restassured.response.Response;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import uk.co.jassoft.markets.datamodel.story.StoryBuilder;
import uk.co.jassoft.markets.repository.StoryRepository;

import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;

/**
 * Created by jonshaw on 25/09/15.
 */
public class StoryControllerTest extends BaseApiTest {

    @Autowired
    private StoryRepository storyRepository;

    @Value("${local.server.port}")
    private int port;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        storyRepository.deleteAll();
    }

    @Test
    public void testGetStoryCountPerDay() throws Exception {

        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusDays(3).toDate()).createStory());
        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusDays(3).toDate()).createStory());
        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusDays(3).toDate()).createStory());
        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusDays(3).toDate()).createStory());
        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusDays(2).toDate()).createStory());
        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusDays(2).toDate()).createStory());
        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusDays(2).toDate()).createStory());
        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusDays(2).toDate()).createStory());
        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusDays(2).toDate()).createStory());
        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusDays(2).toDate()).createStory());
        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusDays(1).toDate()).createStory());
        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusDays(1).toDate()).createStory());

        Response response = given().header(HEADER_SECURITY_TOKEN, adminToken)
                .header(HEADER_SECURITY_EMAIL, adminCredentials.get("email"))
                .header("Accept-Encoding", "gzip, deflate, sdch")
                .contentType(ContentType.JSON)
                .port(port)
                .when().get("/story/countByDay").andReturn();

                System.out.println("Response: " + response.asString());

        ExtractableResponse<Response> extractableResponse = response.then().statusCode(200).extract();

        List<Map<String, String>> list = extractableResponse.jsonPath().get();

        Assert.notNull(list);

        Assert.isTrue(!list.isEmpty());

    }

}