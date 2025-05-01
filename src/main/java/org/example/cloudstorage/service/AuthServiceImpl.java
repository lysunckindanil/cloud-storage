package org.example.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.config.security.CustomUserDetails;
import org.example.cloudstorage.entity.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;

    @Override
    public User authenticateUser(User user) {
        UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken
                .unauthenticated(user.getUsername(), user.getPassword());

        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return ((CustomUserDetails) authentication.getPrincipal()).getUser();
    }

    @Override
    public void putUserInContextAsAuthenticated(User user) {
        UserDetails userDetails = new CustomUserDetails(user);

        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(userDetails, null, userDetails.getAuthorities())
        );
    }
}
