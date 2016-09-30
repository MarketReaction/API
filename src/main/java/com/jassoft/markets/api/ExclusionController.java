/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jassoft.markets.api;

import com.jassoft.markets.datamodel.exclusion.Exclusion;
import com.jassoft.markets.datamodel.exclusion.Exclusions;
import com.jassoft.markets.datamodel.story.NameCount;
import com.jassoft.markets.datamodel.system.Queue;
import com.jassoft.markets.repository.CompanyRepository;
import com.jassoft.markets.repository.ExclusionRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping("/exclusions")
public class ExclusionController extends BaseController
{
    @Autowired
    private ExclusionRepository exclusionRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JmsTemplate jmsTemplateTopic;

    void exclusionAdded(final String message) {
        jmsTemplateTopic.convertAndSend(Queue.ExclusionAdded.toString(), message);
    }

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody Exclusions getExclusions(final HttpServletResponse response) throws UnknownHostException {
        List<Exclusion> exclusions = exclusionRepository.findAll();

        Exclusions exclusionsToReturn = new Exclusions();

        for (Exclusion exclusion : exclusions)
            exclusionsToReturn.add(exclusion);

        response.setHeader("Cache-Control", "no-cache");
        return exclusionsToReturn;
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public @ResponseBody Exclusion addExclusion(final HttpServletResponse response, @RequestBody Exclusion exclusionToAdd) throws UnknownHostException {
        response.setHeader("Cache-Control", "no-cache");

        if(exclusionRepository.findByName(exclusionToAdd.getName()).isEmpty()) {
            Exclusion exclusion = exclusionRepository.save(exclusionToAdd);

            exclusionAdded(exclusion.getName());

            return exclusion;
        }

        return null; //TODO return proper error code
    }

    @RequestMapping(value = "/{exclusionToRemove}/remove", method = RequestMethod.GET)
    public void removeExclusion(final HttpServletResponse response, @PathVariable String exclusionToRemove) throws UnknownHostException {
        response.setHeader("Cache-Control", "no-cache");
        exclusionRepository.delete(exclusionToRemove);
    }

    @RequestMapping(value = "/commonNames/{limit}", method = RequestMethod.GET)
    public @ResponseBody List<NameCount> getCommonNames(final HttpServletResponse response, @PathVariable int limit) throws UnknownHostException {
        List<NameCount> commonNamedEntities = companyRepository.findCommonNamedEntities(new PageRequest(0, limit));

        response.setHeader("Cache-Control", "no-cache");
        return commonNamedEntities;
    }
}
