/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jassoft.markets.api;

import com.jassoft.markets.datamodel.company.Companies;
import com.jassoft.markets.datamodel.company.Company;
import com.jassoft.markets.datamodel.company.CompanyCount;
import com.jassoft.markets.datamodel.system.Queue;
import com.jassoft.markets.datamodel.user.User;
import com.jassoft.markets.repository.CompanyRepository;
import com.jassoft.markets.repository.StorySentimentRepository;
import com.jassoft.markets.repository.UserRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jms.core.JmsTemplate;
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
@RequestMapping("/company")
public class CompanyController extends BaseController
{
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private StorySentimentRepository storySentimentRepository;
    @Autowired
    private UserRepositoryCustom userRepositoryCustom;
    @Autowired
    private JmsTemplate jmsTemplateTopic;

    void foundCompany(final String message) {
        jmsTemplateTopic.convertAndSend(Queue.FoundCompany.toString(), message);
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public @ResponseBody Company getCompany(final HttpServletResponse response, @PathVariable String id) throws UnknownHostException
    {
        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.SIX_HOURS);
        return companyRepository.findOne(id);
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "/{id}/name", method = RequestMethod.GET)
    public @ResponseBody Company getCompanyName(final HttpServletResponse response, @PathVariable String id) throws UnknownHostException
    {
        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.TWENTY_FOUR_HOURS);
        return companyRepository.findOneName(id);
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "exchange/{id}", method = RequestMethod.GET)
    public @ResponseBody Companies getCompanysByExchange(final HttpServletResponse response, @PathVariable String id) throws UnknownHostException
    {
        List<Company> companies = companyRepository.findByExchangeSummary(id);

        Companies companiesToReturn = new Companies();

        for(Company company : companies){
            companiesToReturn.add(company);
        }

        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.THREE_HOURS);
        return companiesToReturn;
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "/ids", method = RequestMethod.POST)
    public @ResponseBody
    Companies getCompanysByIds(final HttpServletResponse response, @RequestBody List<String> companyIds) throws UnknownHostException
    {
        List<Company> companies = (List<Company>) companyRepository.findAllSummary(companyIds);

        Companies companiesToReturn = new Companies();

        for(Company company : companies){
            companiesToReturn.add(company);
        }

        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.TWENTY_FOUR_HOURS);
        return companiesToReturn;
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "exchange/{id}/offset/{offset}/length/{length}", method = RequestMethod.GET)
    public @ResponseBody com.jassoft.markets.datamodel.Page<Company> getCompanysByExchangePageable(final HttpServletResponse response, @PathVariable String id, @PathVariable int offset, @PathVariable int length) throws UnknownHostException
    {
        Page<Company> companies = companyRepository.findByExchangeSummaryPageable(id, new PageRequest((offset / length), length));

        Companies companiesToReturn = new Companies();

        for(Company company : companies.getContent()) {
            companiesToReturn.add(company);
        }

        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.SIX_HOURS);
        return new com.jassoft.markets.datamodel.Page<>((int) companies.getTotalElements(), companiesToReturn);
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "exchange/{id}/search/{searchTerm}/offset/{offset}/length/{length}", method = RequestMethod.GET)
    public @ResponseBody com.jassoft.markets.datamodel.Page<Company> searchCompanysByExchangePageable(final HttpServletResponse response, @PathVariable String id, @PathVariable String searchTerm, @PathVariable int offset, @PathVariable int length) throws UnknownHostException
    {
        Page<Company> companies = companyRepository.searchByExchangeSummaryPageable(id, searchTerm, new PageRequest((offset / length), length));

        Companies companiesToReturn = new Companies();

        for(Company company : companies.getContent()) {
            companiesToReturn.add(company);
        }

        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.SIX_HOURS);
        return new com.jassoft.markets.datamodel.Page<>((int) companies.getTotalElements(), companiesToReturn);
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "search/{name}", method = RequestMethod.GET)
    public @ResponseBody Companies searchByName(final HttpServletResponse response, @PathVariable String name) throws UnknownHostException
    {
        List<Company> companies = companyRepository.findSummaryByName(name, new PageRequest(0, 25));

        Companies companiesToReturn = new Companies();
        
        for(Company company : companies){
            companiesToReturn.add(company);
        }

        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.SIX_HOURS);
        return companiesToReturn;
    }

    @PreAuthorize("isFullyAuthenticated()")
    @RequestMapping(value = "/{id}/follow", method = RequestMethod.GET)
    public @ResponseBody Company followCompany(final HttpServletResponse response, @PathVariable String id) throws UnknownHostException
    {
        Company company = companyRepository.findOne(id);
        User sessionUser = getUser();

        userRepositoryCustom.watchCompany(sessionUser.getId(), company.getId());

        response.setHeader("Cache-Control", "no-cache");
        return company;
    }

    @PreAuthorize("isFullyAuthenticated()")
    @RequestMapping(value = "/{id}/unfollow", method = RequestMethod.GET)
    public @ResponseBody Company unfollowCompany(final HttpServletResponse response, @PathVariable String id) throws UnknownHostException
    {
        Company company = companyRepository.findOne(id);
        User sessionUser = getUser();

        userRepositoryCustom.unwatchCompany(sessionUser.getId(), company.getId());

        response.setHeader("Cache-Control", "no-cache");
        return company;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/{id}/reloadInformation", method = RequestMethod.GET)
    public @ResponseBody Company reloadInformation(final HttpServletResponse response, @PathVariable String id) throws UnknownHostException
    {
        Company company = companyRepository.findOne(id);

        foundCompany(company.getId());

        response.setHeader("Cache-Control", "no-cache");
        return company;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/triggerFind", method = RequestMethod.GET)
    public @ResponseBody void findCompanies() throws UnknownHostException
    {
        jmsTemplateTopic.convertAndSend(Queue.FindCompanies.toString(), "");
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "/{id}/relatedCompanies/{limit}", method = RequestMethod.GET)
    public @ResponseBody List<CompanyCount> getRelatedCompanies(final HttpServletResponse response, @PathVariable String id, @PathVariable int limit) throws UnknownHostException
    {
        List<CompanyCount> relatedCompanies = companyRepository.findRelatedCompanies(id, new PageRequest(0, limit));

        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.TWENTY_FOUR_HOURS);
        return relatedCompanies;
    }
}

class CompanyStorySentiment {
    private String company;
    private String story;
    private Integer sentiment;

    public CompanyStorySentiment(String company, String story, Integer sentiment) {
        this.company = company;
        this.story = story;
        this.sentiment = sentiment;
    }

    public String getCompany() {
        return company;
    }

    public String getStory() {
        return story;
    }

    public Integer getSentiment() {
        return sentiment;
    }
}
