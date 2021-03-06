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
import uk.co.jassoft.markets.datamodel.story.metric.MetricBuilder;
import uk.co.jassoft.markets.datamodel.system.SystemProfile;
import uk.co.jassoft.markets.repository.StoryRepository;
import uk.co.jassoft.markets.repository.SystemProfileRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;

/**
 * Created by jonshaw on 25/09/15.
 */
public class SystemControllerTest extends BaseApiTest {

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private SystemProfileRepository systemProfileRepository;

    @Value("${local.server.port}")
    private int port;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        storyRepository.deleteAll();
    }

    @Test
    public void testGetSlowQueries() throws Exception {
//        systemProfileRepository.save(new SystemProfile("op", "ns", 250000l, 1000l, new Date(), "{query}", 10, 10, false, 0, 0, 0, 0, 1));

        Response response = given().header(HEADER_SECURITY_TOKEN, adminToken)
                .header(HEADER_SECURITY_EMAIL, adminCredentials.get("email"))
                .header("Accept-Encoding", "gzip, deflate, sdch")
                .contentType(ContentType.JSON)
                .port(port)
                .when().get("/system/slowQueries").andReturn();

        System.out.println("Response: " + response.asString());

        ExtractableResponse<Response> extractableResponse = response.then().statusCode(200).extract();

        List<SystemProfile> profiles = extractableResponse.jsonPath().get();

        Assert.notNull(profiles);
    }

    @Test
    public void testGetStoryMetrics() throws Exception {

        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusMinutes(60).toDate())
                .setMetrics(Arrays.asList(MetricBuilder.aSentimentMetric()
                        .withStart(new DateTime().minusMinutes(60).toDate())
                        .withEnd(new DateTime().minusMinutes(30).toDate()).build())
                ).createStory());
        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusMinutes(60).toDate())
                .setMetrics(Arrays.asList(MetricBuilder.aSentimentMetric()
                        .withStart(new DateTime().minusMinutes(60).toDate())
                        .withEnd(new DateTime().minusMinutes(30).toDate()).build())
                ).createStory());
        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusMinutes(60).toDate())
                .setMetrics(Arrays.asList(MetricBuilder.aSentimentMetric()
                        .withStart(new DateTime().minusMinutes(60).toDate())
                        .withEnd(new DateTime().minusMinutes(30).toDate()).build())
                ).createStory());
        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusMinutes(60).toDate())
                .setMetrics(Arrays.asList(MetricBuilder.aSentimentMetric()
                        .withStart(new DateTime().minusMinutes(60).toDate())
                        .withEnd(new DateTime().minusMinutes(30).toDate()).build())
                ).createStory());
        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusMinutes(60).toDate())
                .setMetrics(Arrays.asList(MetricBuilder.aSentimentMetric()
                        .withStart(new DateTime().minusMinutes(60).toDate())
                        .withEnd(new DateTime().minusMinutes(30).toDate()).build())
                ).createStory());
        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusMinutes(60).toDate())
                .setMetrics(Arrays.asList(MetricBuilder.aSentimentMetric()
                        .withStart(new DateTime().minusMinutes(60).toDate())
                        .withEnd(new DateTime().minusMinutes(30).toDate()).build())
                ).createStory());
        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusMinutes(60).toDate())
                .setMetrics(Arrays.asList(MetricBuilder.aSentimentMetric()
                        .withStart(new DateTime().minusMinutes(60).toDate())
                        .withEnd(new DateTime().minusMinutes(30).toDate()).build())
                ).createStory());
        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusMinutes(60).toDate())
                .setMetrics(Arrays.asList(MetricBuilder.aSentimentMetric()
                        .withStart(new DateTime().minusMinutes(60).toDate())
                        .withEnd(new DateTime().minusMinutes(30).toDate()).build())
                ).createStory());
        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusMinutes(60).toDate())
                .setMetrics(Arrays.asList(MetricBuilder.aSentimentMetric()
                        .withStart(new DateTime().minusMinutes(60).toDate())
                        .withEnd(new DateTime().minusMinutes(30).toDate()).build())
                ).createStory());
        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusMinutes(60).toDate())
                .setMetrics(Arrays.asList(MetricBuilder.aSentimentMetric()
                        .withStart(new DateTime().minusMinutes(60).toDate())
                        .withEnd(new DateTime().minusMinutes(30).toDate()).build())
                ).createStory());
        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusMinutes(60).toDate())
                .setMetrics(Arrays.asList(MetricBuilder.aSentimentMetric()
                        .withStart(new DateTime().minusMinutes(60).toDate())
                        .withEnd(new DateTime().minusMinutes(30).toDate()).build())
                ).createStory());
        storyRepository.save(new StoryBuilder().setDatePublished(new DateTime().minusMinutes(60).toDate())
                .setMetrics(Arrays.asList(MetricBuilder.aSentimentMetric()
                        .withStart(new DateTime().minusMinutes(60).toDate())
                        .withEnd(new DateTime().minusMinutes(30).toDate()).build())
                ).createStory());

        Response response = given().header(HEADER_SECURITY_TOKEN, adminToken)
                .header(HEADER_SECURITY_EMAIL, adminCredentials.get("email"))
                .header("Accept-Encoding", "gzip, deflate, sdch")
                .contentType(ContentType.JSON)
                .port(port)
                .when().get("/system/storyMetrics/pastDays/" + 1).andReturn();

                System.out.println("Response: " + response.asString());

        ExtractableResponse<Response> extractableResponse = response.then().statusCode(200).extract();

        Map<String, List<StoryMetricTime>> metrics = extractableResponse.jsonPath().get();

        Assert.notNull(metrics);

        Assert.isTrue(!metrics.isEmpty());

    }

}
