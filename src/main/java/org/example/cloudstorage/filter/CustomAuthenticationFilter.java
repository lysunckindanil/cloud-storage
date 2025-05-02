package org.example.cloudstorage.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.cloudstorage.config.security.CustomUserDetails;
import org.example.cloudstorage.dto.user.UserLoginRequest;
import org.example.cloudstorage.dto.user.UsernameResponseDto;
import org.example.cloudstorage.exception.CustomAuthenticationValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

import static org.example.cloudstorage.util.AuthenticationRequestValidator.validate;

@Component
public class CustomAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final ObjectMapper objectMapper;
    private static final AntPathRequestMatcher AUTHENTICATION_ANT_PATH_REQUEST_MATCHER = new AntPathRequestMatcher("/auth/sign-in", "POST");

    public CustomAuthenticationFilter(ObjectMapper objectMapper) {
        super(AUTHENTICATION_ANT_PATH_REQUEST_MATCHER);
        this.setAuthenticationSuccessHandler();
        this.setAuthenticationFailureHandler();
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
        if (request.getContentType() == null || !request.getContentType().equalsIgnoreCase(MediaType.APPLICATION_JSON_VALUE)) {
            throw new CustomAuthenticationValidationException("Unsupported content type");
        }

        try (InputStream inputStream = request.getInputStream()) {
            UserLoginRequest loginRequest = objectMapper.readValue(inputStream, UserLoginRequest.class);

            validate(loginRequest);

            UsernamePasswordAuthenticationToken authRequest =
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            loginRequest.getUsername(),
                            loginRequest.getPassword());

            authRequest.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            return getAuthenticationManager().authenticate(authRequest);
        } catch (JsonProcessingException e) {
            throw new CustomAuthenticationValidationException("Unsupported JSON format");
        }
    }

    private void setAuthenticationSuccessHandler() {
        super.setAuthenticationSuccessHandler((request, response, authentication) -> {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            UsernameResponseDto usernameResponseDto = new UsernameResponseDto(
                    ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername()
            );
            objectMapper.writeValue(response.getWriter(), usernameResponseDto);
        });
    }

    private void setAuthenticationFailureHandler() {
        super.setAuthenticationFailureHandler((request, response, exception) -> {
            int status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            String message = "Internal Server Error";

            if (exception instanceof CustomAuthenticationValidationException) {
                status = HttpServletResponse.SC_BAD_REQUEST;
                message = exception.getMessage();
            } else if (exception instanceof BadCredentialsException) {
                status = HttpServletResponse.SC_UNAUTHORIZED;
                message = exception.getMessage();
            }

            ProblemDetail problemDetail = ProblemDetail.forStatus(status);
            problemDetail.setProperty("message", message);
            response.setStatus(status);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), problemDetail);
        });
    }


    @Lazy
    @Autowired
    @Override
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }

    @Lazy
    @Autowired
    @Override
    public void setSecurityContextRepository(SecurityContextRepository securityContextRepository) {
        super.setSecurityContextRepository(securityContextRepository);
    }
}
