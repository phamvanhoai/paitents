package com.example.laboratorymanagement.authentication.service;

import com.example.laboratorymanagement.authentication.model.request.AuthenticationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthenticationService {

    @Value("${iam.base-url}")
    private String iamBaseUrl;

    @Value("${app.api-key.name}")
    private String apiKeyHeader;

    @Value("${app.api-key.value}")
    private String apiKeyValue;

    public String verifyToken(AuthenticationRequest authenticationRequest) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add(apiKeyHeader, apiKeyValue);

        HttpEntity<Object> requestEntity =
                new HttpEntity<>(authenticationRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                iamBaseUrl + "/api/internal/auth/verify",
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        return response.getBody();
    }
}
