/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.jassoft.markets.api;

import uk.co.jassoft.markets.datamodel.company.Exchange;
import uk.co.jassoft.markets.datamodel.company.Exchanges;
import uk.co.jassoft.markets.repository.ExchangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/exchange")
public class ExchangeController extends BaseController
{
    @Autowired
    private ExchangeRepository exchangeRepository;

    @Autowired
    private CompanyController companyController;

    @PreAuthorize("permitAll")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public @ResponseBody Exchange getExchange(final HttpServletResponse response, @PathVariable String id) throws UnknownHostException
    {
        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.SIX_HOURS);
        return exchangeRepository.findOne(id);
    }

    @PreAuthorize("permitAll")
    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody Exchanges getExchanges(final HttpServletResponse response) throws UnknownHostException
    {
        List<Exchange> exchanges = exchangeRepository.findByEnabledIsTrue(new Sort(Sort.Direction.ASC, "name"));

        Exchanges exchangesToReturn = new Exchanges();

        for(Exchange exchange : exchanges)
            exchangesToReturn.add(exchange);

        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.SIX_HOURS);
        return exchangesToReturn;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public @ResponseBody Exchanges getAllExchanges(final HttpServletResponse response) throws UnknownHostException
    {
        List<Exchange> exchanges = exchangeRepository.findAll(new Sort(Sort.Direction.ASC, "name"));

        Exchanges exchangesToReturn = new Exchanges();

        for(Exchange exchange : exchanges)
            exchangesToReturn.add(exchange);

        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.FIFTEEN_MINUTES);
        return exchangesToReturn;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/{id}/enable", method = RequestMethod.GET)
    public @ResponseBody Exchange enableExchange(final HttpServletResponse response, @PathVariable String id) throws UnknownHostException
    {
        Exchange exchange = exchangeRepository.findOne(id);

        exchange.setEnabled(true);

        exchange = exchangeRepository.save(exchange);

        companyController.findCompanies();

        response.setHeader("Cache-Control", "no-cache");
        return exchange;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/{id}/disable", method = RequestMethod.GET)
    public @ResponseBody Exchange disableExchange(final HttpServletResponse response, @PathVariable String id) throws UnknownHostException
    {
        Exchange exchange = exchangeRepository.findOne(id);

        exchange.setEnabled(false);

        response.setHeader("Cache-Control", "no-cache");
        return exchangeRepository.save(exchange);
    }
}
