package com.jassoft.markets.api;

import com.jassoft.email.EmailSenderService;
import com.jassoft.markets.BaseSpringConfiguration;
import com.jassoft.markets.api.auth.ApiAuthenticationToken;
import com.jassoft.markets.api.auth.RestAuthenticationEntryPoint;
import com.jassoft.markets.api.auth.TokenAuthenticationManager;
import com.jassoft.markets.repository.UserRepository;
import io.undertow.UndertowOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.ui.velocity.SpringResourceLoader;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by jonshaw on 13/07/15.
 */
@Import({SpringResourceLoader.class})
@Configuration
@ComponentScan("com.jassoft.markets.api")
public class SpringConfiguration extends BaseSpringConfiguration {

    @Bean
    public VelocityEngineFactoryBean velocityEngineFactoryBean() {
        VelocityEngineFactoryBean velocityEngineFactoryBean = new VelocityEngineFactoryBean();
        Properties properties = new Properties();
        properties.setProperty("resource.loader", "class");
        properties.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        velocityEngineFactoryBean.setVelocityProperties(properties);

        return velocityEngineFactoryBean;
    }

    @Bean
    public EmailSenderService emailSenderService() {
        return new EmailSenderService("com/jassoft/email/templates/AccountConfirmation.vm",
                "com/jassoft/email/templates/AccountConfirmationHTML.vm",
                "confirm@market-reaction.com"
                );
    }

    @Bean
    UndertowEmbeddedServletContainerFactory embeddedServletContainerFactory() {
        UndertowEmbeddedServletContainerFactory factory = new UndertowEmbeddedServletContainerFactory();
        factory.addBuilderCustomizers(
                builder -> builder.setServerOption(UndertowOptions.ENABLE_HTTP2, true));
        return factory;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SpringConfiguration.class, args);
    }
}

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserRepository userRepository;

    private RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint();

    @Override
    protected void configure(HttpSecurity http) throws Exception {

       http
            .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/user/register").permitAll()
                .antMatchers(HttpMethod.POST, "/user/register/confirm").permitAll()
                .antMatchers(HttpMethod.POST, "/user/authenticate").permitAll()
                .antMatchers(HttpMethod.POST, "/user/oauth2/google").permitAll()
                .antMatchers("/**").authenticated()
        .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
                .anonymous()
        .and()
                .securityContext()
        .and()
                .headers().disable()
                .rememberMe().disable()
                .requestCache().disable()
                .x509().disable()
                .csrf().disable()
                .httpBasic().disable()
                .formLogin().disable()
                .logout().disable()
        .addFilterBefore(authenticationFilter(), BasicAuthenticationFilter.class)
        .exceptionHandling().authenticationEntryPoint(entryPoint);

    }

    public AuthenticationFilter authenticationFilter() {
        List<String> ignoredPaths = new ArrayList<>();
        ignoredPaths.add("/user/register");
        ignoredPaths.add("/user/register/confirm");
        ignoredPaths.add("/user/authenticate");
        ignoredPaths.add("/user/oauth2/google");

        return new AuthenticationFilter(new TokenAuthenticationManager(userRepository),
                entryPoint,
                ignoredPaths);
    }

}

 class AuthenticationFilter extends GenericFilterBean {

    private final static String HEADER_SECURITY_TOKEN = "X-AuthToken";
    private final static String HEADER_SECURITY_EMAIL = "X-AuthEmail";

    private final AuthenticationManager authenticationManager;

    private final RestAuthenticationEntryPoint authenticationEntryPoint;

    private final List<String> ignoredPaths;

    public AuthenticationFilter(AuthenticationManager authenticationManager, RestAuthenticationEntryPoint authenticationEntryPoint, List<String> ignoredPaths) {
        this.authenticationManager = authenticationManager;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.ignoredPaths = ignoredPaths;
    }

    @Override
     public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;

        if(!ignoredPaths.contains(request.getServletPath())) {

            try {

                String email = request.getHeader(HEADER_SECURITY_EMAIL);
                String token = request.getHeader(HEADER_SECURITY_TOKEN);

                Authentication authentication = new ApiAuthenticationToken(email, token);

                authentication = authenticationManager.authenticate(authentication);

                if (authentication == null || !authentication.isAuthenticated())
                    throw new AuthenticationServiceException(MessageFormat.format("Error | {0}", "Bad Token"));

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (AuthenticationException failed) {
                SecurityContextHolder.clearContext();

                HttpServletResponse response = (HttpServletResponse) res;

                authenticationEntryPoint.commence(request, response, failed);

                return;
            }
        }

        chain.doFilter(req, res);

     }
 }