/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jassoft.markets.api;

import com.jassoft.markets.datamodel.prediction.Prediction;
import com.jassoft.markets.repository.PredictionRepository;
import com.jassoft.markets.repository.PredictionRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Jonny
 */
@RestController
@RequestMapping("/prediction")
public class PredictionController extends BaseController
{
    @Autowired
    private PredictionRepository predictionRepository;

    @Autowired
    private PredictionRepositoryCustom predictionRepositoryCustom;

    @PreAuthorize("permitAll")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public @ResponseBody
    Prediction getPrediction(final HttpServletResponse response, @PathVariable String id)
    {
        response.setHeader("Cache-Control", "max-age=" + CacheTimeout.THREE_HOURS);
        return predictionRepository.findOne(id);
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "/company/{id}", method = RequestMethod.GET)
    public @ResponseBody
    List<Prediction> getPredictionsByCompany(final HttpServletResponse response, @PathVariable String id)
    {
        response.setHeader("Cache-Control", "no-cache");
        return predictionRepository.findByCompany(id, new PageRequest(0, 30, new Sort(Sort.Direction.DESC, "predictionDate"))).getContent();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "latest/{limit}/certainty/{certainty}/", method = RequestMethod.GET)
    public @ResponseBody
    List<Prediction> getLatestPredictions(final HttpServletResponse response, @PathVariable int limit, @PathVariable float certainty)
    {
        response.setHeader("Cache-Control", "no-cache");
        return predictionRepository.findByCertaintyGreaterThanEqual(new Double(certainty), new PageRequest(0, limit, new Sort(Sort.Direction.DESC, "predictionDate"))).getContent();
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "correctness/certainty/{certainty}/", method = RequestMethod.GET)
    public @ResponseBody
    List<Correctness> getPredictionCorrectness(final HttpServletResponse response, @PathVariable Double certainty)
    {
        response.setHeader("Cache-Control", "no-cache");

        return predictionRepositoryCustom.getPredictionAccuracyByDayAndCertainty(certainty).stream()
                .map(dateFloatPair -> new Correctness(dateFloatPair.getLeft(), dateFloatPair.getRight()))
                .collect(Collectors.toList());
    }

    @PreAuthorize("permitAll")
    @RequestMapping(value = "correctness/company/{id}", method = RequestMethod.GET)
    public @ResponseBody
    List<Correctness> getPredictionCorrectness(final HttpServletResponse response, @PathVariable String id)
    {
        response.setHeader("Cache-Control", "no-cache");

        return predictionRepositoryCustom.getPredictionAccuracyByDayAndByCompany(id).stream()
                .map(dateFloatPair -> new Correctness(dateFloatPair.getLeft(), dateFloatPair.getRight()))
                .collect(Collectors.toList());
    }
}

class Correctness {
    private Date date;
    private Float correctness;

    public Correctness(Date date, Float correctness) {
        this.date = date;
        this.correctness = correctness;
    }

    public Date getDate() {
        return date;
    }

    public Float getCorrectness() {
        return correctness;
    }
}
