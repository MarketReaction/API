/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jassoft.markets.api;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.jassoft.email.EmailSenderService;
import com.jassoft.markets.datamodel.error.ApiError;
import com.jassoft.markets.datamodel.user.OAuth2Provider;
import com.jassoft.markets.datamodel.user.User;
import com.jassoft.markets.datamodel.user.UserBuilder;
import com.jassoft.markets.datamodel.user.Users;
import com.jassoft.markets.exceptions.user.*;
import com.jassoft.markets.repository.UserRepository;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * @author Jonny
 */
@RestController
@RequestMapping("/user")
public class UserController  extends BaseController {
    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailSenderService emailSenderService;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @PreAuthorize("isFullyAuthenticated()")
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public
    @ResponseBody
    User getCurrentUser(final HttpServletResponse response) throws UnknownHostException
    {
        User user = getUser();

        response.setHeader("Cache-Control", "no-cache");
        return user.clean();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    User getUser(final HttpServletResponse response, @PathVariable String id) throws UnknownHostException {
        response.setHeader("Cache-Control", "no-cache");
        return userRepository.findOne(id).clean();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/{id}/role/{role}", method = RequestMethod.GET)
    public
    @ResponseBody
    User assignRoleToUser(final HttpServletResponse response, @PathVariable String id, @PathVariable String role) throws UnknownHostException {
        response.setHeader("Cache-Control", "no-cache");
        User user = userRepository.findOne(id);

        user.getRoles().add(role);

        return userRepository.save(user).clean();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public
    @ResponseBody
    Users getUsers(final HttpServletResponse response) throws UnknownHostException {
        List<User> users = userRepository.findAll();

        Users usersToReturn = new Users();

        for (User user : users)
            usersToReturn.add(user.clean());

        response.setHeader("Cache-Control", "no-cache");
        return usersToReturn;
    }

    @PreAuthorize("isAnonymous()")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public
    @ResponseBody
    User register(final HttpServletResponse response, @RequestBody User user) throws UnknownHostException, UserException {
        if (userRepository.findByEmail(user.getEmail()) != null)
            throw new UserExistsException("User Exists with Email " + user.getEmail());

        user.setPassword(encoder.encode(user.getPassword()));
        user.setActivationId(UUID.randomUUID().toString());

        user = userRepository.save(user);

        try {
            Map model = new HashMap();
            model.put("user", user);

            emailSenderService.send(user.getEmail(), "Market Reaction Account Confirmation", model);
        } catch (Exception exception) {
            LOG.error("Failed to send user confirmation email", exception);

            throw new UserConfirmationEmailFailedException(exception.getMessage());
        }

        response.setHeader("Cache-Control", "no-cache");
        return user.clean();
    }

    @PreAuthorize("isAnonymous()")
    @RequestMapping(value = "/register/confirm", method = RequestMethod.POST)
    public
    @ResponseBody
    User confirm(final HttpServletResponse response, @RequestBody String activationId) throws UnknownHostException, UserException {
        User user = userRepository.findByActivationId(activationId);

        if (user == null)
            throw new UnknownActivationCodeException("Activation Code " + activationId + " is not recognised");

        user.setActivated(true);

        userRepository.save(user);

        response.setHeader("Cache-Control", "no-cache");
        return user.clean();
    }

    @PreAuthorize("isAnonymous()")
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public
    @ResponseBody
    User authenticate(final HttpServletResponse response, @RequestBody Map<String, String> credentials) throws UnknownHostException, UserException {
        User user = userRepository.findByEmail(credentials.get("email"));

        if (user == null)
            throw new UserNotExistsException("User does not Exist with Email " + credentials.get("email"));

        if (!encoder.matches(credentials.get("password"), user.getPassword()))
            throw new UserIncorrectCredentialsException("Credentials not valid for Email " + credentials.get("email"));

        if (!user.isActivated())
            throw new UserNotActivatedException("User not activated with Email " + credentials.get("email"));

        user.setToken(UUID.randomUUID().toString());
        user.setTokenExpiry(new DateTime(DateTimeZone.UTC).plusHours(24).toDate());
        user.setLastLogin(new Date());

        user = userRepository.save(user);

        response.setHeader("Cache-Control", "no-cache");
        return user.clean();
    }

    @PreAuthorize("isFullyAuthenticated()")
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public
    @ResponseBody
    User logout(final HttpServletResponse response) throws UnknownHostException
    {
        User user = getUser();

        user.setToken(null);
        user.setTokenExpiry(new Date());

        user = userRepository.save(user);

        response.setHeader("Cache-Control", "no-cache");
        return user.clean();
    }

    @PreAuthorize("isFullyAuthenticated()")
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    public
    @ResponseBody
    User updatePassword(final HttpServletResponse response, @RequestBody Map<String, String> passwords) throws UnknownHostException, UserException {

        User user = getUser();

        if (!encoder.matches(passwords.get("currentPassword"), user.getPassword()))
            throw new UserIncorrectCredentialsException("Current Password Invalid");

        if(!passwords.get("newPassword").equals(passwords.get("newPasswordConfirm")))
            throw new UserIncorrectCredentialsException("New passwords do not match");

        user.setPassword(encoder.encode(passwords.get("newPassword")));

        user = userRepository.save(user);

        response.setHeader("Cache-Control", "no-cache");
        return user.clean();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/{id}/enable", method = RequestMethod.GET)
    public
    @ResponseBody
    User enableUser(final HttpServletResponse response, @PathVariable String id) throws UnknownHostException
    {
        User user = userRepository.findOne(id);

        user.setActivated(true);

        userRepository.save(user);

        response.setHeader("Cache-Control", "no-cache");
        return user.clean();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/{id}/disable", method = RequestMethod.GET)
    public
    @ResponseBody
    User disableUser(final HttpServletResponse response, @PathVariable String id) throws UnknownHostException
    {
        User user = userRepository.findOne(id);

        user.setActivated(false);

        userRepository.save(user);

        response.setHeader("Cache-Control", "no-cache");
        return user.clean();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/{id}/delete", method = RequestMethod.GET)
    public
    @ResponseBody
    void deleteUser(final HttpServletResponse response, @PathVariable String id) throws UnknownHostException
    {
        response.setHeader("Cache-Control", "no-cache");
        userRepository.delete(id);
    }

    @PreAuthorize("isAnonymous()")
    @RequestMapping(value = "/oauth2/google", method = RequestMethod.POST)
    public
    @ResponseBody
    User oauth2(final HttpServletResponse response, @RequestBody String token) throws IOException, UserException, GeneralSecurityException {


        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Arrays.asList("1019531568622-2q3l48lebdnm3hn15vis5h42ilod4r38.apps.googleusercontent.com"))
                // If you retrieved the token on Android using the Play Services 8.3 API or newer, set
                // the issuer to "https://accounts.google.com". Otherwise, set the issuer to
                // "accounts.google.com". If you need to verify tokens from multiple sources, build
                // a GoogleIdTokenVerifier for each issuer and try them both.
                .setIssuer("accounts.google.com")
                .build();

        // (Receive idTokenString by HTTPS POST)

        GoogleIdToken idToken = verifier.verify(token);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            // Print user identifier
//            String userId = payload.getSubject();
//            System.out.println("User ID: " + userId);

            // Get profile information from payload
            String email = payload.getEmail();
            boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
            String familyName = (String) payload.get("family_name");
            String givenName = (String) payload.get("given_name");
            Date expiry = new Date(((Long) payload.get("exp")) * 1000 );
            Date loggedIn = new Date(((Long) payload.get("iat")) * 1000 );

            if(emailVerified) {

                User user = userRepository.findByEmail(email);

                if (user == null) {
                    // TODO - Create user
                    user = userRepository.save(UserBuilder.anUser()
                            .withEmail(email)
                            .withForename(givenName)
                            .withSurname(familyName)
                            .withActivated(true)
                            .withOAuth2Provider(OAuth2Provider.GOOGLE)
                            .build());
                }

                if(user.getoAuth2Provider() == null || !user.getoAuth2Provider().equals(OAuth2Provider.GOOGLE)) {
                    throw new UserExistsException("User Exists with Email " + user.getEmail());
                }

                user.setToken(UUID.randomUUID().toString());
                user.setTokenExpiry(expiry);
                user.setLastLogin(loggedIn);

                user = userRepository.save(user);

                response.setHeader("Cache-Control", "no-cache");
                return user.clean();
            }

        }

        throw new UserIncorrectCredentialsException("Invalid ID token");

    }

    @ExceptionHandler({UserException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleException(Exception exception) {
        if (exception instanceof UserExistsException)
            return new ApiError(101, exception.getMessage());

        if (exception instanceof UnknownActivationCodeException)
            return new ApiError(103, exception.getMessage());

        if (exception instanceof UserNotExistsException)
            return new ApiError(104, exception.getMessage());

        if (exception instanceof UserIncorrectCredentialsException)
            return new ApiError(105, exception.getMessage());

        if (exception instanceof UserNotActivatedException)
            return new ApiError(106, exception.getMessage());

        if (exception instanceof UserConfirmationEmailFailedException)
            return new ApiError(107, exception.getMessage());

        return new ApiError(999, "Unknown Error [" + exception.toString() + "]");
    }
}
