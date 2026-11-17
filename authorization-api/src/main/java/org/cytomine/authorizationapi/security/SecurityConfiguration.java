package org.cytomine.authorizationapi.security;

import lombok.RequiredArgsConstructor;
import org.cytomine.common.security.CommonSecurityConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final CommonSecurityConfiguration commonSecurityConfiguration;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return commonSecurityConfiguration.commonHttp(http).build();
    }
}
