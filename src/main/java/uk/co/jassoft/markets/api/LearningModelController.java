/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.jassoft.markets.api;

import uk.co.jassoft.markets.repository.LearningModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Jonny
 */
@RestController
@RequestMapping("/learningModel")
public class LearningModelController extends BaseController
{
    @Autowired
    private LearningModelRepository learningModelRepository;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/size", method = RequestMethod.GET)
    public @ResponseBody long getLearningModelSize(final HttpServletResponse response)
    {
        response.setHeader("Cache-Control", "no-cache");
        return learningModelRepository.count();
    }

}
