/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.jassoft.markets.api;

import uk.co.jassoft.markets.datamodel.story.date.DateFormat;
import uk.co.jassoft.markets.datamodel.story.date.DateFormats;
import uk.co.jassoft.markets.datamodel.story.date.MissingDateFormat;
import uk.co.jassoft.markets.datamodel.story.date.MissingDateFormats;
import uk.co.jassoft.markets.repository.DateFormatRepository;
import uk.co.jassoft.markets.repository.MissingDateFormatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.UnknownHostException;
import java.util.List;

/**
 *
 * @author Jonny
 */
@RestController
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping("/dateFormats")
public class DateFormatController extends BaseController
{
    @Autowired
    private DateFormatRepository dateFormatRepository;

    @Autowired
    private MissingDateFormatRepository missingDateFormatRepository;

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody DateFormats getDateFormats(final HttpServletResponse response) throws UnknownHostException {
        List<DateFormat> dateFormats = dateFormatRepository.findAll();

        DateFormats dateFormatsToReturn = new DateFormats();

        for (DateFormat dateFormat : dateFormats)
            dateFormatsToReturn.add(dateFormat);

        response.setHeader("Cache-Control", "no-cache");
        return dateFormatsToReturn;
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public @ResponseBody DateFormat addDateFormat(final HttpServletResponse response, @RequestBody DateFormat dateFormatToAdd) throws UnknownHostException {
        response.setHeader("Cache-Control", "no-cache");
        return dateFormatRepository.save(dateFormatToAdd);
    }

    @RequestMapping(value = "/{dateFormatToRemove}/remove", method = RequestMethod.GET)
    public void removeDateFormat(final HttpServletResponse response, @PathVariable String dateFormatToRemove) throws UnknownHostException {
        response.setHeader("Cache-Control", "no-cache");
        dateFormatRepository.delete(dateFormatToRemove);
    }

    @RequestMapping(value = "/missing", method = RequestMethod.GET)
    public @ResponseBody
    MissingDateFormats getMissingDateFormats(final HttpServletResponse response) throws UnknownHostException {
        List<MissingDateFormat> missingDateFormats = missingDateFormatRepository.findAll();

        MissingDateFormats missingDateFormatsToReturn = new MissingDateFormats();

        for (MissingDateFormat missingDateFormat : missingDateFormats)
            missingDateFormatsToReturn.add(missingDateFormat);

        response.setHeader("Cache-Control", CacheTimeout.FIFTEEN_MINUTES);
        return missingDateFormatsToReturn;
    }
}
