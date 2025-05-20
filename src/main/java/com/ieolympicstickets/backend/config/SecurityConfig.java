package com.ieolympicstickets.backend.config;

import com.ieolympicstickets.backend.security.JwtAuthenticationFilter;
import com.ieolympicstickets.backend.security.JwtService;
import com.ieolympicstickets.backend.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    // ─── JWT ─────────────────────────────────────────────────────────────────────
    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Value("${JWT_EXPIRATION_MS}")
    private long jwtExpirationMs; // fallback 1h


    // ─── CORS ORIGINS (depuis application.properties) ───────────────────────────
    // application.properties doit contenir :
    // cors.allowed-origins=${ALLOWED_ORIGINS:http://localhost:8081}
    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    // ─── ENCODER ─────────────────────────────────────────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ─── AUTHENTICATION MANAGER (JPA UserService) ────────────────────────────────
    @Bean
    public AuthenticationManager authenticationManager(
            UserService userService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    // ─── JWT SERVICE & FILTER ─────────────────────────────────────────────────────
       @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService) {
        return new JwtAuthenticationFilter(jwtService);
    }


    /**
     * Empêche Spring Boot d'enregistrer ce filtre en tant que Filter global.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> disableJwtAutoRegistration(
            JwtAuthenticationFilter filter
    ) {
        FilterRegistrationBean<JwtAuthenticationFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setEnabled(false);
        return reg;
    }



    // ─── CHAÎNE DE SÉCURITÉ ───────────────────────────────────────────────────────
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthFilter
    ) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        // auth publique
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()

                        // lecture publique
                        .requestMatchers(HttpMethod.GET,
                                "/api/offers/**", "/api/events/**", "/api/events", "/api/offers").permitAll()
                        .requestMatchers("/error").permitAll()

                        // CRUD événements/offres → ADMIN only
                        .requestMatchers(HttpMethod.POST,
                                "/api/events/**", "/api/offers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/events/**", "/api/offers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/events/**", "/api/offers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/events/**", "/api/offers/**").hasRole("ADMIN")
                        // 1) Création / lecture / modification de panier autorisées sans login
                        .requestMatchers(HttpMethod.POST,   "/api/cart",           "/api/cart/items").permitAll()
                        .requestMatchers(HttpMethod.GET,    "/api/cart/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH,  "/api/cart/items/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/cart/items/**").permitAll()

                        // validation panier → user authentifié
                        .requestMatchers(HttpMethod.POST,
                                "/api/cart/validate").authenticated()

                        // Actuator → ADMIN only
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // tout le reste → authentifié par défaut
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ─── CONFIGURATION CORS ───────────────────────────────────────────────────────
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        cfg.setAllowedOrigins(Arrays.asList(allowedOrigins));
        cfg.setAllowedMethods(List.of(
                "GET","POST","PUT","PATCH","DELETE","OPTIONS"));

        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","X-Session-Id"));
        cfg.setExposedHeaders(List.of("Authorization"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource src =
                new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}


