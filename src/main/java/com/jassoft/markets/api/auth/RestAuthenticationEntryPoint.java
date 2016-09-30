package com.jassoft.markets.api.auth;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Jonny on 14/08/2014.
 */
//@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint
{
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException ) throws IOException, ServletException
    {
        response.sendError( HttpServletResponse.SC_UNAUTHORIZED, String.format( "Unauthorized - %s" , authException.getLocalizedMessage() ));
    }

}