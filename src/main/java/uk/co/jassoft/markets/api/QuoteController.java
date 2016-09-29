package uk.co.jassoft.markets.api;

import uk.co.jassoft.markets.datamodel.CompanyDirection;
import uk.co.jassoft.markets.datamodel.Direction;
import uk.co.jassoft.markets.datamodel.company.quote.Quote;
import uk.co.jassoft.markets.datamodel.company.quote.Quotes;
import uk.co.jassoft.markets.repository.QuoteRepository;
import uk.co.jassoft.markets.utils.QuoteUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.UnknownHostException;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by Jonny on 03/09/2014.
 */
@RestController
@RequestMapping("/quote")
public class QuoteController extends BaseController
{
    private static final Logger LOG = LoggerFactory.getLogger(QuoteController.class);

    @Autowired
    private QuoteRepository quoteRepository;

    @PreAuthorize("permitAll")
    @RequestMapping(value = "company/{id}", method = RequestMethod.GET)
    public @ResponseBody
    Quotes getQuotesByCompany(final HttpServletResponse response, @PathVariable String id) throws UnknownHostException
    {
        List<Quote> quotes = quoteRepository.findByCompanyAndIntraday(id, false, new PageRequest(0, 30, new Sort(Sort.Direction.ASC, "date")));

        Quotes quotesToReturn = new Quotes();

        for(Quote quote : quotes) {
            quotesToReturn.add(quote);
        }

        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.THREE_HOURS);
        return quotesToReturn;
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "company/{id}/intraday", method = RequestMethod.POST)
    public @ResponseBody
    List<Quote> getIntradayQuotesByCompany(final HttpServletResponse response, @PathVariable String id, @RequestBody Map<String, Date> dates) throws UnknownHostException
    {
        List<Quote> quotes = quoteRepository.findByCompanyAndIntradayAndDateBetween(id, true, dates.get("from"), dates.get("to"));

        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.THREE_HOURS);
        return quotes;
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "company/{id}/today", method = RequestMethod.GET)
    public @ResponseBody
    CompanyQuote getTodaysQuoteByCompany(final HttpServletResponse response, @PathVariable String id) throws UnknownHostException
    {
        List<Quote> quotes = quoteRepository.findByCompanyAndIntraday(id, false, new PageRequest(0, 1, new Sort(Sort.Direction.ASC, "date")));

        Optional<Quote> todaysQuote = quotes.stream().filter(isToday()).findFirst();

        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.THREE_HOURS);
        return new CompanyQuote(id, todaysQuote.isPresent() ? todaysQuote.get().getClose() : null);
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "direction/company/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    CompanyDirection getSentimentDirectionForCompany(final HttpServletResponse response, @PathVariable String id) {

        List<Quote> quotes = quoteRepository.findByCompanyAndIntraday(id, false, new PageRequest(0, 2, new Sort(Sort.Direction.ASC, "date")));

        Direction direction = Direction.None;

        try {
            direction = QuoteUtils.getPreviousPriceDirection(quotes);
        }
        catch (Exception exception) {
            LOG.warn("Failed to calculate Sentiment Direction", exception);
        }

        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.FIFTEEN_MINUTES);
        return new CompanyDirection(id, direction);
    }

    public static Predicate<Quote> isToday() {
        return s -> DateUtils.truncate(new Date(), Calendar.DATE).equals(DateUtils.truncate(s.getDate(), Calendar.DATE));
    }

    class CompanyQuote {
        private String companyId;
        private Double quote;

        public CompanyQuote(String companyId, Double quote) {
            this.companyId = companyId;
            this.quote = quote;
        }

        public String getCompanyId() {
            return companyId;
        }

        public Double getQuote() {
            return quote;
        }
    }
}
