package be.cytomine.config;

import lombok.RequiredArgsConstructor;
import org.cytomine.common.security.CommonSecurityConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import be.cytomine.config.security.ApiKeyFilter;
import be.cytomine.mapper.UserMapper;
import be.cytomine.repository.security.UserRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CommonSecurityConfiguration commonSecurityConfiguration;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return commonSecurityConfiguration.commonHttp(http)
            // Deprecated. Kept as transitional in 2024.2
            .addFilterBefore(new ApiKeyFilter(userRepository, userMapper), BasicAuthenticationFilter.class)
            .authorizeHttpRequests((authorizeHttpRequests) ->
                authorizeHttpRequests
                    .requestMatchers("/api/abstractimage/**").permitAll()
                    .requestMatchers("/api/meilisearch/**").permitAll()
                    .requestMatchers("/api/imageinstance/**").permitAll()
                    .requestMatchers("/api/uploadedfile/*/download").permitAll()
                    .requestMatchers("/api/userannotation/**").permitAll()

                    .requestMatchers("/api/**").authenticated()
                    .requestMatchers("/session/admin/info.json").authenticated()
                    .requestMatchers("/session/admin/open.json").authenticated()
                    .requestMatchers("/session/admin/close.json").authenticated()

                    // TODO 2024.2 - LAST CONNECTION (IN A PROJECT)
                    .requestMatchers(HttpMethod.GET, "/server/ping").permitAll()
                    .requestMatchers("/**").permitAll() // TODO IAM: remove ?
            ).build();
    }
}
