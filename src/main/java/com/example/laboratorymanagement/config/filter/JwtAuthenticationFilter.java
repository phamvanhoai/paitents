package com.example.laboratorymanagement.config.filter;

import com.example.laboratorymanagement.authentication.model.request.AuthenticationRequest;
import com.example.laboratorymanagement.authentication.model.response.AuthenticationResponse;
import com.example.laboratorymanagement.authentication.model.response.AuthenticationResponseData;
import com.example.laboratorymanagement.authentication.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = getJwtFromRequest(request);

        if (jwt == null || jwt.isBlank()
                || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            AuthenticationRequest authenticationRequest = new AuthenticationRequest();
            authenticationRequest.setToken(jwt);

            // ⭐⭐⭐ CALL ĐÚNG HÀM verifyToken
            String data = authenticationService.verifyToken(authenticationRequest);

            ObjectMapper objectMapper = new ObjectMapper();
            AuthenticationResponse authenticationResponse =
                    objectMapper.readValue(data, AuthenticationResponse.class);

            AuthenticationResponseData authenticationResponseData =
                    authenticationResponse.getData();

            if (authenticationResponseData == null) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }

            Set<String> privilegeCodes = authenticationResponseData.getPrivilegeCodes();
            List<GrantedAuthority> authorities = new ArrayList<>();

            if (privilegeCodes != null) {
                for (String privilegeCode : privilegeCodes) {
                    authorities.add(new SimpleGrantedAuthority(privilegeCode));
                }
            }

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            authenticationResponseData.getUserId(),
                            authenticationResponseData.getRoleCode(),
                            authorities
                    );

            authenticationToken.setDetails(authenticationResponseData);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(request, response);

        } catch (HttpClientErrorException e) {

            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED
                    || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                response.setStatus(e.getStatusCode().value());
                return;
            }

            throw e;
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
