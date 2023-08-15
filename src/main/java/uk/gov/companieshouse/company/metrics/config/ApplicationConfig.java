package uk.gov.companieshouse.company.metrics.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import uk.gov.companieshouse.company.metrics.auth.EricTokenAuthenticationFilter;
import uk.gov.companieshouse.company.metrics.logging.DataMapHolder;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.logging.Logger;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ApplicationConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private Logger logger;

    @Bean
    EnvironmentReader environmentReader() {
        return new EnvironmentReaderImpl();
    }

    /**
     * Configure Http Security.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        this.logger.debug("Start: configure Http Security", DataMapHolder.getLogMap());
        http.httpBasic().disable()
                //REST APIs not enabled for cross site script headers
                .csrf().disable() //NOSONAR
                .formLogin().disable()
                .logout().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterAt(new EricTokenAuthenticationFilter(logger),
                        BasicAuthenticationFilter.class)
                .authorizeRequests()
                .anyRequest().permitAll();
        this.logger.debug("End: configure Http Security", DataMapHolder.getLogMap());
    }

    /**
     * Exclude health check endpoint from security filter.
     */
    @Override
    public void configure(WebSecurity web) {
        this.logger.debug("Start: Excluding health check endpoint from security filter", DataMapHolder.getLogMap());
        web.ignoring().antMatchers("/company-metrics-api/healthcheck");
        this.logger.debug("End: Excluding health check endpoint from security filter", DataMapHolder.getLogMap());
    }
}
