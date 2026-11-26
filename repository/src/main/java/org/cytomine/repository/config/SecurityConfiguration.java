package org.cytomine.repository.config;

import lombok.RequiredArgsConstructor;
import org.cytomine.common.config.security.JwtAuthConverter;
import org.cytomine.common.security.CommonSecurityConfiguration;
import org.cytomine.repository.config.security.ApiKeyFilter;
import org.cytomine.repository.persistence.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final JwtAuthConverter customJwtAuthConverter;
    private final UserRepository userRepository;
    private final CommonSecurityConfiguration commonSecurityConfiguration;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return commonSecurityConfiguration.commonHttp(http)
            .addFilterBefore(new ApiKeyFilter(userRepository), BasicAuthenticationFilter.class)
            .authorizeHttpRequests(
                authorizeHttpRequests -> authorizeHttpRequests.requestMatchers("/ping").permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll().anyRequest().authenticated())
            .oauth2ResourceServer((oauth2) -> oauth2.jwt(
                jwtAuthConverter -> jwtAuthConverter.jwtAuthenticationConverter(customJwtAuthConverter))).build();

    }
}
