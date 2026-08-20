package be.cytomine.config.security;

import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import be.cytomine.mapper.UserMapper;
import be.cytomine.repository.security.UserRepository;

@Deprecated
public class ApiKeyConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    public ApiKeyConfigurer(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public void configure(HttpSecurity http) {
        ApiKeyFilter customFilter = new ApiKeyFilter(userRepository, userMapper);
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
