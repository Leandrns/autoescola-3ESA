package br.com.fiap3esa.autoescola3esa.infra.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors
                        .configurationSource( request -> {
                            CorsConfiguration configuration = new CorsConfiguration();
                            configuration.setAllowedOriginPatterns(Arrays
                                    .asList(
                                            "https://dominiodomeufront.com.br",
                                            "http://128.10.0.58/meufront",
                                            "http://localhost:3000",
                                            "http://127.0.0.1:3000",
                                            "http://localhost:5500",
                                            "http://127.0.0.1:5500"));
                            configuration.setAllowedMethods(List.of(
                                    "GET",
                                    "POST",
                                    "PUT",
                                    "PATCH",
                                    "DELETE",
                                    "OPTIONS",
                                    "HEAD")
                            );
                            configuration.setAllowedHeaders(List.of(
                                    "Authorization",
                                    "Content-Type",
                                    "Accept",
                                    "Origin"
                            ));
                            configuration.setAllowCredentials(true);
                            return configuration;
                        }
                ))
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login").permitAll()
                        .requestMatchers(
                                "/v3/api/api-docs.yaml",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**").permitAll()
                        /*.requestMatchers(HttpMethod.POST, "/instrutores").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/instrutores").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/instrutores/{id}").hasRole("ADMIN")*/
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        securityFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}