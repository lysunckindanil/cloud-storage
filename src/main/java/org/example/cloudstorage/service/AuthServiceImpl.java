package org.example.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.config.security.CustomUserDetails;
import org.example.cloudstorage.entity.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextHolderStrategy securityContextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();

    @Override
    public User putUserInContextWithAuthentication(User user) {
        UsernamePasswordAuthenticationToken token =
                UsernamePasswordAuthenticationToken.unauthenticated(user.getUsername(), user.getPassword());

        Authentication authentication = authenticationManager.authenticate(token);
        System.out.println(authentication.isAuthenticated());
        setAuthenticationContext(authentication);
        return ((CustomUserDetails) authentication.getPrincipal()).getUser();
    }

    @Override
    public void putUserInContextWithoutAuthentication(User user) {
        UserDetails userDetails = new CustomUserDetails(user);
        setAuthenticationContext(UsernamePasswordAuthenticationToken.authenticated(
                userDetails, null, userDetails.getAuthorities()
        ));
    }

    private void setAuthenticationContext(Authentication authentication) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new AuthenticationServiceException("No request attributes available");
        }

        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);

        securityContextRepository.saveContext(context, attributes.getRequest(), attributes.getResponse());
    }
}