package org.example.cloudstorage.config.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.filter.CheckAuthenticationBeforeLogoutFilter;
import org.example.cloudstorage.filter.CustomAuthenticationFilter;
import org.example.cloudstorage.filter.RegistrationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomAuthenticationFilter customAuthenticationFilter;
    private final RegistrationFilter registrationFilter;
    private final CheckAuthenticationBeforeLogoutFilter checkAuthenticationBeforeLogoutFilter;
    private final SecurityContextRepository contextRepo;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(checkAuthenticationBeforeLogoutFilter, LogoutFilter.class)
                .addFilterAt(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(registrationFilter, UsernamePasswordAuthenticationFilter.class)
                .securityContext(context -> context.securityContextRepository(contextRepo))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .anyRequest().authenticated();
    }

    private void exceptionHandlingConf(ExceptionHandlingConfigurer<HttpSecurity> handling) {
        handling.authenticationEntryPoint(
                (request, response, ex)
                        -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
        );
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost");
        config.addAllowedOrigin("http://frontend");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}