package com.example.booking.service;

import com.example.booking.dto.LoginRequest;
import com.example.booking.dto.LoginResponse;
import com.example.booking.security.JwtUtil;
import com.example.booking.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    public LoginResponse login(LoginRequest request) {
        // Delegates to the DaoAuthenticationProvider configured in SecurityConfig,
        // which loads the user via CustomUserDetailsService and checks the BCrypt hash.
        // Throws BadCredentialsException on mismatch, handled by GlobalExceptionHandler.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        @SuppressWarnings("unchecked")
        List<? extends GrantedAuthority> authorities = (List<? extends GrantedAuthority>) authentication.getAuthorities();

        String token = jwtUtil.generateToken(principal.getUsername(), principal.getId(), authorities);

        String role = authorities.get(0).getAuthority().replace("ROLE_", "");

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(principal.getUsername())
                .role(role)
                .expiresInMs(expirationMs)
                .build();
    }
}
