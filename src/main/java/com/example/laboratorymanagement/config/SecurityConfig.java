package com.example.laboratorymanagement.config;

import com.example.laboratorymanagement.config.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {}) // dùng CorsConfig của bạn

                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/api/internal/**").permitAll()
                        .requestMatchers("/api/test/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/").permitAll()


                        // ==========================
                        // ⚠️ PATIENT API — CHECK QUYỀN
                        // ==========================

                        // XEM bệnh nhân
                        .requestMatchers(HttpMethod.GET, "/api/patients/**")
                        .hasAuthority("VIEW_PATIENT")

                        // THÊM bệnh nhân
                        .requestMatchers(HttpMethod.POST, "/api/patients/**")
                        .hasAuthority("ADD_PATIENT")

                        // SỬA bệnh nhân
                        .requestMatchers(HttpMethod.PUT, "/api/patients/**")
                        .hasAuthority("MODIFY_PATIENT")

                        // XÓA bệnh nhân
                        .requestMatchers(HttpMethod.DELETE, "/api/patients/**")
                        .hasAuthority("DELETE_PATIENT")

                        // Các request khác → chỉ cần authenticated
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
