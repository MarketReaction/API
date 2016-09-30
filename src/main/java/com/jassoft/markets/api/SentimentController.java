/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jassoft.markets.api;

import com.jassoft.markets.datamodel.CompanyDirection;
import com.jassoft.markets.datamodel.Direction;
import com.jassoft.markets.datamodel.company.Company;
import com.jassoft.markets.datamodel.company.sentiment.EntitySentiment;
import com.jassoft.markets.datamodel.company.sentiment.PeriodType;
import com.jassoft.markets.datamodel.company.sentiment.SentimentByDate;
import com.jassoft.markets.datamodel.company.sentiment.StorySentiment;
import com.jassoft.markets.repository.CompanyRepository;
import com.jassoft.markets.repository.StorySentimentRepository;
import com.jassoft.markets.utils.SentimentUtil;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * @author Jonny
 */
@RestController
@RequestMapping("/sentiment")
public class SentimentController extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(SentimentController.class);

    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private StorySentimentRepository storySentimentRepository;

    private Date getTrincatedDate(PeriodType periodType, Date date) {
        //TODO this needs to be implemented for different PeriodTypes
        return DateUtils.truncate(date, Calendar.DATE);
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "company/{id}/story/{storyId}", method = RequestMethod.GET)
    public @ResponseBody CompanyStorySentiment getCompanyStorySentiment(final HttpServletResponse response, @PathVariable String id, @PathVariable String storyId) throws UnknownHostException
    {
        List<StorySentiment> storySentiments = storySentimentRepository.findByCompany(id);

        Integer sentiment = null;

        // This can sometimes be null if the 1st story matched to a company hasnt yet been sentiment analysed
        if(storySentiments != null) {
            sentiment = storySentiments.stream()
                    .map(storySentiment -> storySentiment.getEntitySentiment().stream().collect(Collectors.summingInt(EntitySentiment::getSentiment)))
                    .collect(Collectors.summingInt(value -> value));
        }

        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.TWENTY_FOUR_HOURS);
        return new CompanyStorySentiment(id, storyId, sentiment);
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "company/{id}/period/{period}", method = RequestMethod.GET)
    public
    @ResponseBody
    List<SentimentByDate> getSentimentsByCompany(final HttpServletResponse response, @PathVariable String id, @PathVariable PeriodType period) {

        // This could do some mongoDB magic to only select the StorySentiments in the date
        List<StorySentiment> storySentiments = storySentimentRepository.findByCompany(id);

        List<SentimentByDate> companySentiments = storySentiments.stream()
                .sorted((s1, s2) -> s1.getStoryDate().compareTo(s2.getStoryDate()))
                .map(storySentiment -> new SentimentByDate(id, storySentiment.getStoryDate(), storySentiment.getEntitySentiment().stream().collect(Collectors.summingInt(value -> value.getSentiment()))))
                .collect(Collectors.groupingBy(sentimentByDate -> getTrincatedDate(period, sentimentByDate.getDate()), Collectors.summingInt(value1 -> value1.getSentiment())))
                .entrySet().stream().map(dateIntegerEntry -> new SentimentByDate(id, dateIntegerEntry.getKey(), dateIntegerEntry.getValue())).sorted((o1, o2) -> o1.getDate().compareTo(o2.getDate()))
                .collect(Collectors.toList());

        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.FIFTEEN_MINUTES);
        return companySentiments;
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "company/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    SentimentByDate getCurrentSentimentsByCompany(final HttpServletResponse response, @PathVariable String id) {

        // This could do some mongoDB magic to only select the StorySentiments in the date range
        List<StorySentiment> storySentiments = storySentimentRepository.findByCompany(id);

        List<SentimentByDate> companySentiments = storySentiments.stream()
                .filter(isToday())
                .sorted((s1, s2) -> s1.getStoryDate().compareTo(s2.getStoryDate()))
                .map(storySentiment -> new SentimentByDate(id, storySentiment.getStoryDate(), storySentiment.getEntitySentiment().stream().collect(Collectors.summingInt(value -> value.getSentiment()))))
                .collect(Collectors.groupingBy(sentimentByDate -> getTrincatedDate(PeriodType.Day, sentimentByDate.getDate()), Collectors.summingInt(value1 -> value1.getSentiment())))
                .entrySet().stream().map(dateIntegerEntry -> new SentimentByDate(id, dateIntegerEntry.getKey(), dateIntegerEntry.getValue())).sorted((o1, o2) -> o1.getDate().compareTo(o2.getDate()))
                .collect(Collectors.toList());

        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.FIFTEEN_MINUTES);
        return companySentiments.isEmpty() ? new SentimentByDate(id, new Date(), null) : companySentiments.get(0);
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "direction/company/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    CompanyDirection getSentimentDirectionForCompany(final HttpServletResponse response, @PathVariable String id) {

        List<StorySentiment> storySentiments = storySentimentRepository.findByCompany(id);

        Direction direction = null;

        try {
            direction = SentimentUtil.getPreviousSentimentDirection(storySentiments, new Date());
        }
        catch (Exception exception) {
            LOG.debug("Failed to calculate Sentiment Direction for comapny {}", id);
        }

        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.FIFTEEN_MINUTES);
        return new CompanyDirection(id, direction);
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "{direction}/period/{period}/limit/{limit}", method = RequestMethod.GET)
    public
    @ResponseBody
    List<CompanySentiment> getChartToday(final HttpServletResponse response, @PathVariable String direction, @PathVariable PeriodType period, @PathVariable int limit) throws UnknownHostException {

        List<StorySentiment> storySentiments = storySentimentRepository.findByStoryDateGreaterThan(DateUtils.truncate(new Date(), Calendar.DATE));

        List<CompanySentiment> todayCompanySentiments = storySentiments.stream()
                .map(storySentiment -> new ImmutablePair<>(storySentiment.getCompany(), storySentiment.getEntitySentiment().stream().collect(Collectors.summingInt(value -> value.getSentiment()))))
                .collect(Collectors.groupingBy(pair -> pair.getKey(), Collectors.summingInt(value -> value.getValue())))
                .entrySet().stream()
                .map(stringIntegerEntry -> {
                    Company company = companyRepository.findOne(stringIntegerEntry.getKey());
                    return new CompanySentiment(stringIntegerEntry.getKey(), company.getName(), stringIntegerEntry.getValue());
                })
                .sorted((o1, o2) -> {
                    switch (direction) {
                        case "highest":
                            return Integer.compare(o2.getSentiment(), o1.getSentiment());

                        case"lowest":
                        default:
                            return Integer.compare(o1.getSentiment(), o2.getSentiment());
                    }
                })
                .limit(limit)
                .collect(Collectors.toList());

        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.FIFTEEN_MINUTES);
        return todayCompanySentiments;
    }

    public static Predicate<StorySentiment> isToday() {
        return s -> DateUtils.truncate(new Date(), Calendar.DATE).equals(DateUtils.truncate(s.getStoryDate(), Calendar.DATE));
    }

    public static Predicate<StorySentiment> forCompany(String company) {
        return s -> s.getCompany().equals(company);
    }

    class CompanySentiment {
        private String companyId;
        private String companyName;
        private Integer sentiment;

        public CompanySentiment(String companyId, String companyName, Integer sentiment) {
            this.companyId = companyId;
            this.companyName = companyName;
            this.sentiment = sentiment;
        }

        public String getCompanyId() {
            return companyId;
        }

        public String getCompanyName() {
            return companyName;
        }

        public Integer getSentiment() {
            return sentiment;
        }
    }

}