/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jassoft.markets.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jassoft.markets.datamodel.story.Story;
import com.jassoft.markets.datamodel.system.SystemProfile;
import com.jassoft.markets.repository.StoryRepository;
import com.jassoft.markets.repository.SystemProfileRepository;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 *
 * @author Jonny
 */
@RestController
@RequestMapping("/system")
public class SystemController extends BaseController
{
    private static final Logger LOG = LoggerFactory.getLogger(SystemController.class);

    @Autowired
    private SystemProfileRepository systemProfileRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Value("${ACTIVEMQ_PORT_61616_TCP_ADDR:activemq}")
    private String activeMQHost;

    @Value("${ACTIVEMQ_PORT_61616_TCP_PORT:61616}")
    private int activeMQPort;

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper mapper = new ObjectMapper();

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/slowQueries", method = RequestMethod.GET)
    public @ResponseBody List<SystemProfile> getSlowQueries(final HttpServletResponse response)
    {
        response.setHeader("Cache-Control", "no-cache");
        return systemProfileRepository.findAll(new PageRequest(0, 25, Sort.Direction.DESC, "ts")).getContent();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public @ResponseBody String getStatus(final HttpServletResponse response)
    {
        response.setHeader("Cache-Control", "no-cache");
        return "Healthy";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/storyMetrics/pastDays/{days}", method = RequestMethod.GET)
    public @ResponseBody Map<String, List<StoryMetricTime>> getStoryMetrics(final HttpServletResponse response, final @PathVariable int days)
    {
        List<Story> stories = storyRepository.findMetricsBetweenDates(new DateTime().minusDays(days).toDate(), new Date());

        Map<String, List<StoryMetricTime>> stageMetrics = new ConcurrentHashMap<>();

        stories.stream()
                .map(Story::getMetrics)
                .flatMap(metrics -> metrics.stream())
                .filter(metric -> metric.getStart() != null && metric.getEnd() != null)
                .forEach(metric -> {
                    stageMetrics.computeIfAbsent(metric.getName(), s -> new ArrayList<StoryMetricTime>())
                    .add(new StoryMetricTime(metric.getStart(), new DateTime(metric.getEnd()).minus(metric.getStart().getTime()).toDate().getTime()));
                });


        response.setHeader("Cache-Control", "no-cache");
        return stageMetrics;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/queues", method = RequestMethod.GET)
    public @ResponseBody List<String> getQueues(final HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-cache");
//        http://activemq.apache.org/jmx.html
//        http://localhost:32783/api/jolokia/read/org.apache.activemq:type=Broker,brokerName=localhost

        String plainCreds = "admin:admin";
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);

        String url = String.format("http://%s:%s/api/jolokia/read/org.apache.activemq:type=Broker,brokerName=localhost", activeMQHost, activeMQPort);

        LOG.info("Requesting Queues from url [{}]", url);

        HttpEntity<String> request = new HttpEntity<String>(headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        JsonNode root = mapper.readTree(responseEntity.getBody());

        JsonNode queues = root.get("value").get("Queues");

        List<String> objectnames = queues.findValuesAsText("objectName");

        return objectnames.parallelStream().map(objectname ->
                objectname.replace("org.apache.activemq:brokerName=localhost,destinationName=", "").replace(",destinationType=Queue,type=Broker", "")
        ).collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/queue/{destinationType}/{destinationName}", method = RequestMethod.GET)
    public @ResponseBody Map<String, Object> getQueueStats(final HttpServletResponse response, @PathVariable String destinationType, @PathVariable String destinationName) throws IOException {

        response.setHeader("Cache-Control", "no-cache");

        String plainCreds = "admin:admin";
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);

        String url = String.format("http://%s:%s/api/jolokia/read/org.apache.activemq:type=Broker,brokerName=localhost,destinationName=%s,destinationType=%s", activeMQHost, activeMQPort, destinationName, destinationType);

        LOG.info("Requesting Queue Stats from url [{}]", url);

        HttpEntity<String> request = new HttpEntity<String>(headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        JsonNode root = mapper.readTree(responseEntity.getBody());

        JsonNode queue = root.get("value");

        if(queue == null) {
            return new HashMap<>();
        }

        return mapper.convertValue(queue, Map.class);
    }

}

class StoryMetricTime {
    private Date date;
    private Long processingTime;

    public StoryMetricTime(Date date, Long processingTime) {
        this.date = date;
        this.processingTime = processingTime;
    }

    public Date getDate() {
        return date;
    }

    public Long getProcessingTime() {
        return processingTime;
    }
}
