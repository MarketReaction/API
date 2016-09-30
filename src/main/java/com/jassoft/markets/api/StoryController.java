/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jassoft.markets.api;

import com.jassoft.markets.datamodel.story.Story;
import com.jassoft.markets.repository.StoryRepository;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Jonny
 */
@RestController
@RequestMapping("/story")
public class StoryController extends BaseController
{
    @Autowired
    private StoryRepository storyRepository;

    @PreAuthorize("permitAll")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public @ResponseBody Story getStory(final HttpServletResponse response, @PathVariable String id) throws UnknownHostException
    {
        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.SIX_HOURS);
        return storyRepository.findOne(id);
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "latest/published/{limit}", method = RequestMethod.GET)
    public @ResponseBody List<Story> getLatestPublishedStories(final HttpServletResponse response, @PathVariable int limit) throws UnknownHostException
    {
        Page<Story> stories = storyRepository.findAll(new PageRequest(0, limit, Sort.Direction.DESC, "datePublished"));

        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.FIFTEEN_MINUTES);
        return stories.getContent();
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "latest/found/{limit}", method = RequestMethod.GET)
    public @ResponseBody List<Story> getLatestFoundStories(final HttpServletResponse response, @PathVariable int limit) throws UnknownHostException
    {
        Page<Story> stories = storyRepository.findAll(new PageRequest(0, limit, Sort.Direction.DESC, "dateFound"));

        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.FIFTEEN_MINUTES);
        return stories.getContent();
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "/ids", method = RequestMethod.POST)
    public @ResponseBody List<Story> getLatestFoundStories(final HttpServletResponse response, @RequestBody List<String> companyIds)
    {
        List<Story> stories = storyRepository.findByMatchedCompaniesIn(companyIds, new PageRequest(0, 25, Sort.Direction.DESC, "datePublished"));

        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.FIFTEEN_MINUTES);
        return stories;
    }

    @RequestMapping(value = "/source/{id}", method = RequestMethod.GET)
    public @ResponseBody List<Story> getLatestStories(final HttpServletResponse response, @PathVariable final String id) throws UnknownHostException
    {
        List<Story> stories = storyRepository.findByParentSource(id, new PageRequest(0, 10, Sort.Direction.DESC, "datePublished"));

        response.setHeader("Cache-Control", "no-cache");
        return stories;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/countByDay", method = RequestMethod.GET)
    public @ResponseBody List<StoryDate> getStoryCountByDay(final HttpServletResponse response)
    {
        List<Pair<Date, Integer>> storyCounts = storyRepository.getStoryCountPerDay(new DateTime().minusDays(14).toDate(), new Date());

        response.setHeader("Cache-Control", "no-cache");

        return storyCounts.stream().map(dateIntegerPair -> new StoryDate(dateIntegerPair.getKey(), dateIntegerPair.getValue())).collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/processingTimes/pastDays/{days}", method = RequestMethod.GET)
    public @ResponseBody List<StoryProcessingTime> getProcessingTimesInPastDays(final HttpServletResponse response, @PathVariable int days)
    {
        List<Triple<String, Date, Long>> storyCounts = storyRepository.getStoryProcessingTimes(new DateTime().minusDays(days).toDate(), new Date());

        response.setHeader("Cache-Control", "no-cache");

        return storyCounts.stream().map(stringDateLongTriple -> new StoryProcessingTime(stringDateLongTriple.getLeft(), stringDateLongTriple.getMiddle(), stringDateLongTriple.getRight())).collect(Collectors.toList());
    }
}

class StoryDate {
    private final Date date;
    private final int stories;

    public StoryDate(Date date, int stories) {
        this.date = date;
        this.stories = stories;
    }

    public Date getDate() {
        return date;
    }

    public int getStories() {
        return stories;
    }
}

class StoryProcessingTime {
    private final String story;
    private final Date date;
    private final Long processingTime;

    public StoryProcessingTime(String story, Date date, Long processingTime) {
        this.story = story;
        this.date = date;
        this.processingTime = processingTime;
    }

    public String getStory() {
        return story;
    }

    public Date getDate() {
        return date;
    }

    public Long getProcessingTime() {
        return processingTime;
    }
}