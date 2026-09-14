package org.cytomine.repository.config;

import jakarta.servlet.http.HttpServletResponse;
import org.cytomine.repository.config.security.ApiKeyFilter;
import org.cytomine.repository.persistence.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import be.cytomine.common.config.security.JwtAuthConverter;
import be.cytomine.common.config.security.TokenFromParameterFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final JwtAuthConverter customJwtAuthConverter;
    private final UserRepository userRepository;

    public SecurityConfiguration(JwtAuthConverter customJwtAuthConverter, UserRepository userRepository) {
        this.customJwtAuthConverter = customJwtAuthConverter;
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .addFilterBefore(new ApiKeyFilter(userRepository), BasicAuthenticationFilter.class)
            .exceptionHandling((exceptionHandling) ->
                exceptionHandling
                    .authenticationEntryPoint(
                        (request, response, authException) ->
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
            )
            .authorizeHttpRequests((authorizeHttpRequests) ->
                authorizeHttpRequests
                    .requestMatchers("/ping").permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .anyRequest().authenticated()
            );
        http
            .addFilterBefore(new TokenFromParameterFilter(), BearerTokenAuthenticationFilter.class)
            .oauth2ResourceServer((oauth2) -> oauth2
                .jwt(jwtAuthConverter -> jwtAuthConverter.jwtAuthenticationConverter(customJwtAuthConverter)));
        return http.build();
    }
}
