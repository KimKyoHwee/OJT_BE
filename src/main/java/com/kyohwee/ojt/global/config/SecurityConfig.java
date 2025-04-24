package com.kyohwee.ojt.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import java.util.Arrays;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;


import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /*
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy(
                """
                            ROLE_ADMIN > ROLE_MENTEE
                            ROLE_ADMIN > ROLE_MENTOR
                            ROLE_MENTEE > ROLE_USER
                            ROLE_MENTOR > ROLE_USER
                        """);
        return hierarchy;
    }

     */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(
                        cors ->
                                cors.configurationSource(
                                        request -> {
                                            CorsConfiguration configuration =
                                                    new CorsConfiguration();

                                            configuration.setAllowedOrigins(
                                                    Arrays.asList(
                                                            "https://localhost:3000",
                                                            "http://localhost:3000",
                                                            "http://localhost:8080"));

                                            configuration.setAllowedMethods(
                                                    Arrays.asList(
                                                            "GET", "POST", "PUT", "DELETE", "PATCH",
                                                            "OPTIONS"));
                                            configuration.setAllowedHeaders(
                                                    Collections.singletonList("*"));
                                            configuration.setExposedHeaders(
                                                    Arrays.asList(
                                                            "Set-Cookie",
                                                            "Authorization",
                                                            "Access",
                                                            "loginStatus"));
                                            configuration.setAllowCredentials(true);
                                            configuration.setMaxAge(3600L);
                                            return configuration;
                                        }))
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .formLogin(formLogin -> formLogin.disable()) // 폼 로그인 비활성화
                .httpBasic(httpBasic -> httpBasic.disable()) // HTTP Basic 인증 비활성화
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                                        .permitAll()
                                        .requestMatchers(
                                                "/v3/api-docs/**",
                                                "/v3/api-docs.yaml",     // ✅ 이걸로 수정해야 Redoc 정상 동작
                                                "/swagger-ui/**",
                                                "/swagger-resources/**",
                                                "/redoc.html",
                                                "/webjars/**",
                                                "/favicon.ico",
                                                "/health-check",
                                                "/auth/reissue/**",
                                                "/security-check",
                                                "/reissue",
                                                "/docs/**",
                                                "/",
                                                "/api/**"
                                        ).permitAll()


                                        /*
                                        .permitAll()
                                        .requestMatchers(HttpMethod.GET, "/admin/issue")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET, "/api/v2/mentors/{mentorId}/**")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.GET, "/api/v2/mentors/part")
                                        .permitAll()
                                        .requestMatchers("/auth/reissue/mobile/**")
                                        .permitAll()
                                        .requestMatchers("/auth/issue/mobile/**")
                                        .permitAll()
                                        .requestMatchers("/api/v2/possibleDates/**")
                                        .hasAnyRole("MENTOR", "MENTEE")
                                        .requestMatchers("/api/v2/mentors/**")
                                        .hasAnyRole("MENTOR", "MENTEE")
                                        .requestMatchers("/api/v2/applications/**")
                                        .hasAnyRole("MENTOR", "MENTEE")
                                         */
                                        .anyRequest()
                                        .authenticated())

                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
