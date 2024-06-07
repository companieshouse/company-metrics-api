package uk.gov.companieshouse.company.metrics.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import uk.gov.companieshouse.company.metrics.auth.EricTokenAuthenticationFilter;
import uk.gov.companieshouse.company.metrics.logging.DataMapHolder;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.logging.Logger;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ApplicationConfig {

    @Autowired
    private Logger logger;

    @Bean
    EnvironmentReader environmentReader() {
        return new EnvironmentReaderImpl();
    }

    /**
     * Configure Http Security.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, Logger logger) throws Exception {
        this.logger.debug("Start: configure Http Security", DataMapHolder.getLogMap());
        http.httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAt(new EricTokenAuthenticationFilter(logger),
                        BasicAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        this.logger.debug("End: configure Http Security", DataMapHolder.getLogMap());
        return http.build();
    }

    /**
     * Configure Web Security,Exclude health check endpoint from security filter.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        this.logger.debug("Start: Excluding health check endpoint from security filter",
                DataMapHolder.getLogMap());
        return web -> web.ignoring().requestMatchers("/company-metrics-api/healthcheck");
    }
}
