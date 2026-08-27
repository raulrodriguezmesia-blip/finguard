package com.ai.fintech.observability.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // ---------- Password Encoder ----------
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ---------- Authentication Manager (exponer como bean si se necesita manualmente) ----------
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // ---------- Security Filter Chain ----------
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF: desactivado para APIs stateless (se puede activar con SameSite si se usa cookies)
            .csrf(csrf -> csrf.disable())
            // Sesiones: stateless para JWT u otro token
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Autorización de rutas
            .authorizeHttpRequests(authz -> authz
                // Actuator y Swagger accesibles sin auth (ajustar según necesidad)
                .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Resto de la API requiere rol ADMIN o USER
                .requestMatchers("/api/**").hasAnyRole("ADMIN", "USER")
                .anyRequest().authenticated()
            )
            // HTTP Basic (para demo) o reemplazar por JWT filter
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // ---------- CORS Configuration ----------
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://tu-dominio.com")); // ajustar
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // ---------- UserDetailsService (ejemplo en memoria) ----------
    // En producción, sustituir por JDBC, LDAP o OAuth2UserService.
    @Bean
    public org.springframework.security.core.userdetails.UserDetailsService userDetailsService(PasswordEncoder encoder) {
        org.springframework.security.core.userdetails.UserDetails admin =
                org.springframework.security.core.userdetails.User.withUsername("admin")
                        .password(encoder.encode("CambiarEstaClave123!")) // cambiar en vault o variable de entorno
                        .roles("ADMIN")
                        .build();

        org.springframework.security.core.userdetails.UserDetails user =
                org.springframework.security.core.userdetails.User.withUsername("usuario")
                        .password(encoder.encode("CambiarEstaClave123!")) // cambiar
                        .roles("USER")
                        .build();

        return new org.springframework.security.core.userdetails.InMemoryUserDetailsManager(admin, user);
    }
}
