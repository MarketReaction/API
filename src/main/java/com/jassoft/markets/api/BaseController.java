package com.jassoft.markets.api;

import com.jassoft.markets.datamodel.user.User;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Created by jonshaw on 30/01/15.
 */
public abstract class BaseController {

    protected interface CacheTimeout { // In Seconds
        String FIFTEEN_MINUTES = "900";
        String THREE_HOURS = "10800";
        String SIX_HOURS = "21600";
        String TWENTY_FOUR_HOURS = "86400";
    }

    protected User getUser()
    {
        try {
            Object principle = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (principle instanceof User)
                return (User) principle;
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }

        return null;
    }
}
