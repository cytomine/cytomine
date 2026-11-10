package org.cytomine.common.security;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.cytomine.common.config.security.TokenFromParameterFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@RequiredArgsConstructor
@Component
public class CommonSecurityConfiguration {

    private final JwtAuthConverter customJwtAuthConverter;

    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Content-Disposition"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    public HttpSecurity commonHttp(HttpSecurity http) {
        return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .addFilterBefore(new TokenFromParameterFilter(), BearerTokenAuthenticationFilter.class)
            .addFilterBefore(new IncomingAuthorizationFilter(), BearerTokenAuthenticationFilter.class)
            .exceptionHandling((exceptionHandling) ->
                exceptionHandling
                    .authenticationEntryPoint(
                        (request, response, authException) ->
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
            )
            .oauth2ResourceServer((oauth2) -> oauth2
                .jwt(jwtAuthConverter -> jwtAuthConverter.jwtAuthenticationConverter(customJwtAuthConverter)));

    }
}
