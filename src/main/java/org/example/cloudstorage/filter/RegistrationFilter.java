package org.example.cloudstorage.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.cloudstorage.config.security.CustomUserDetails;
import org.example.cloudstorage.dto.user.UserRegisterRequest;
import org.example.cloudstorage.dto.user.UsernameResponseDto;
import org.example.cloudstorage.exception.CustomAuthenticationValidationException;
import org.example.cloudstorage.exception.UserWithThisNameAlreadyExistsException;
import org.example.cloudstorage.service.UserService;
import org.example.cloudstorage.util.UserMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

import static org.example.cloudstorage.util.AuthenticationRequestValidator.validate;

@Component
public class RegistrationFilter extends AbstractAuthenticationProcessingFilter {
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;
    private final UserService userService;
    private static final AntPathRequestMatcher AUTHENTICATION_ANT_PATH_REQUEST_MATCHER = new AntPathRequestMatcher("/auth/sign-up", "POST");

    public RegistrationFilter(SecurityContextRepository contextRepository, AuthenticationManager authenticationManager, ObjectMapper objectMapper, UserService userService, UserMapper userMapper) {
        super(AUTHENTICATION_ANT_PATH_REQUEST_MATCHER);
        this.setSecurityContextRepository(contextRepository);
        this.setAuthenticationManager(authenticationManager);
        this.setAuthenticationSuccessHandler();
        this.setAuthenticationFailureHandler();
        this.objectMapper = objectMapper;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
        if (request.getContentType() == null || !request.getContentType().equalsIgnoreCase(MediaType.APPLICATION_JSON_VALUE)) {
            throw new CustomAuthenticationValidationException("Unsupported content type");
        }

        try (InputStream inputStream = request.getInputStream()) {
            UserRegisterRequest registerRequest = objectMapper.readValue(inputStream, UserRegisterRequest.class);
            validate(registerRequest);
            CustomUserDetails userDetails = new CustomUserDetails(userService.register(userMapper.toUser(registerRequest)));
            return UsernamePasswordAuthenticationToken.authenticated(userDetails, null, userDetails.getAuthorities());

        } catch (JsonProcessingException e) {
            throw new CustomAuthenticationValidationException("Unsupported JSON format");
        }
    }

    private void setAuthenticationSuccessHandler() {
        super.setAuthenticationSuccessHandler((request, response, authentication) -> {
            response.setStatus(HttpServletResponse.SC_CREATED);
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
            } else if (exception instanceof UserWithThisNameAlreadyExistsException) {
                status = HttpServletResponse.SC_CONFLICT;
                message = exception.getMessage();
            }

            ProblemDetail problemDetail = ProblemDetail.forStatus(status);
            problemDetail.setProperty("message", message);
            response.setStatus(status);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), problemDetail);
        });
    }
}