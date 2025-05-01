package org.example.cloudstorage.config.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


@EnableRedisHttpSession
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityContext(context -> context.securityContextRepository(contextRepo()))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(this::exceptionHandlingConf)
                .authorizeHttpRequests(this::requestsMatchersConf)
                .logout(this::logoutConf);
        return http.build();
    }

    private void logoutConf(LogoutConfigurer<HttpSecurity> http) {
        http
                .logoutUrl("/auth/sign-out")
                .logoutSuccessHandler((
                        (request, response, authentication)
                                -> response.setStatus(HttpServletResponse.SC_NO_CONTENT)));
    }

    private void requestsMatchersConf(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorize) {
        authorize
                .requestMatchers("/auth/sign-in", "/auth/sign-up").anonymous()
                .anyRequest().authenticated();
    }

    private void exceptionHandlingConf(ExceptionHandlingConfigurer configurer) {
        configurer.authenticationEntryPoint(
                (request, response, ex)
                        -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
        );
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextRepository contextRepo() {
        return new HttpSessionSecurityContextRepository();
    }
}