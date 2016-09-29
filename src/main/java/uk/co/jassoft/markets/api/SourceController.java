/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.jassoft.markets.api;

import uk.co.jassoft.markets.datamodel.sources.Source;
import uk.co.jassoft.markets.datamodel.sources.SourceUrl;
import uk.co.jassoft.markets.datamodel.sources.Sources;
import uk.co.jassoft.markets.repository.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Jonny
 */
@RestController
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping("/source")
public class SourceController extends BaseController
{
    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    protected MongoTemplate mongoTemplate;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public @ResponseBody Source getSource(final HttpServletResponse response, @PathVariable final String id) throws UnknownHostException
    {
        response.setHeader("Cache-Control", "no-cache");
        return sourceRepository.findOne(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody Sources getSources(final HttpServletResponse response) throws UnknownHostException
    {
        List<Source> sources = sourceRepository.findAll();
        
        Sources sourcesToReturn = new Sources();
        
        for(Source source : sources)
            sourcesToReturn.add(source);

        response.setHeader("Cache-Control", "no-cache");
        return sourcesToReturn;
    }
    
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public @ResponseBody Source addSource(final HttpServletResponse response, @RequestBody final Source sourceToAdd) throws UnknownHostException
    {
        response.setHeader("Cache-Control", "no-cache");
        return sourceRepository.save(sourceToAdd);
    }

    @RequestMapping(value = "/{id}/exclusion/add", method = RequestMethod.POST)
    public @ResponseBody Source addExclusion(final HttpServletResponse response, @PathVariable final String id, @RequestBody final String exclusion) throws UnknownHostException
    {
        response.setHeader("Cache-Control", "no-cache");
        return sourceRepository.pushExclusion(id, exclusion);
    }

    @RequestMapping(value = "/{id}/url/add", method = RequestMethod.POST)
    public @ResponseBody Source addUrl(final HttpServletResponse response, @PathVariable final String id, @RequestBody final String url) throws UnknownHostException
    {
        response.setHeader("Cache-Control", "no-cache");
        return sourceRepository.pushUrl(id, url);
    }

    @RequestMapping(value = "/{id}/url/enable", method = RequestMethod.POST)
    public @ResponseBody Source enableUrl(final HttpServletResponse response, @PathVariable final String id, @RequestBody final String url) throws UnknownHostException
    {
        Source source = sourceRepository.findOne(id);

        Optional<SourceUrl> sourceUrlOptional = source.getUrls().stream()
                .filter(sourceUrl -> sourceUrl.getUrl().equals(url))
                .findFirst();

        if(sourceUrlOptional.isPresent()) {
            SourceUrl sourceUrl = sourceUrlOptional.get();
            sourceUrl.setEnabled(true);

            mongoTemplate.updateFirst(Query.query(Criteria.where("id").is(source.getId()).and("urls.url").is(url)),
                    new Update().set("urls.$", sourceUrl)
                    , Source.class);
        }

        response.setHeader("Cache-Control", "no-cache");
        return sourceRepository.findOne(id);
    }

    @RequestMapping(value = "/{id}/url/disable", method = RequestMethod.POST)
    public @ResponseBody Source disableUrl(final HttpServletResponse response, @PathVariable final String id, @RequestBody final String url) throws UnknownHostException
    {
        Source source = sourceRepository.findOne(id);

        Optional<SourceUrl> sourceUrlOptional = source.getUrls().stream()
                .filter(sourceUrl -> sourceUrl.getUrl().equals(url))
                .findFirst();

        if(sourceUrlOptional.isPresent()) {
            SourceUrl sourceUrl = sourceUrlOptional.get();
            sourceUrl.setEnabled(false);

            mongoTemplate.updateFirst(Query.query(Criteria.where("id").is(source.getId()).and("urls.url").is(url)),
                    new Update().set("urls.$", sourceUrl)
                    , Source.class);
        }

        response.setHeader("Cache-Control", "no-cache");
        return sourceRepository.findOne(id);
    }
    
    @RequestMapping(value = "/{id}/exclusion/remove", method = RequestMethod.POST)
    public @ResponseBody Source removeExclusion(final HttpServletResponse response, @PathVariable final String id, @RequestBody final String exclusion) throws UnknownHostException
    {
        response.setHeader("Cache-Control", "no-cache");
        return sourceRepository.pullExclusion(id, exclusion);
    }
    
    @RequestMapping(value = "/{id}/url/remove", method = RequestMethod.POST)
    public @ResponseBody Source removeUrl(final HttpServletResponse response, @PathVariable final String id, @RequestBody final String url) throws UnknownHostException
    {
        response.setHeader("Cache-Control", "no-cache");
        return sourceRepository.pullUrl(id, url);
    }

    @RequestMapping(value = "/{id}/enable", method = RequestMethod.GET)
    public @ResponseBody Source enableExchange(final HttpServletResponse response, @PathVariable final String id) throws UnknownHostException
    {
        Source source = sourceRepository.findOne(id);

        source.setDisabled(false);

        response.setHeader("Cache-Control", "no-cache");
        return sourceRepository.save(source);
    }

    @RequestMapping(value = "/{id}/disable", method = RequestMethod.GET)
    public @ResponseBody Source disableExchange(final HttpServletResponse response, @PathVariable final String id) throws UnknownHostException
    {
        Source source = sourceRepository.findOne(id);

        source.setDisabled(true);

        response.setHeader("Cache-Control", "no-cache");
        return sourceRepository.save(source);
    }
}
