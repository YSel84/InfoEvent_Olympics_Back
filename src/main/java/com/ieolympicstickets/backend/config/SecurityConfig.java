package com.ieolympicstickets.backend.config;

import com.ieolympicstickets.backend.security.JwtAuthenticationFilter;
import com.ieolympicstickets.backend.security.JwtService;
import com.ieolympicstickets.backend.service.UserService;
import org.springframework.beans.factory.annotation.Value;
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

    // ─── USERS ADMIN / FRONT ─────────────────────────────────────────────────────
    @Value("${ADMIN_USERNAME}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Value("${FRONT_USERNAME}")
    private String frontUsername;

    @Value("${FRONT_PASSWORD}")
    private String frontPassword;

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
                                "/api/offers/**", "/api/events/**").permitAll()

                        // CRUD événements/offres → ADMIN only
                        .requestMatchers(HttpMethod.POST,
                                "/api/events/**", "/api/offers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/events/**", "/api/offers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/events/**", "/api/offers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/events/**", "/api/offers/**").hasRole("ADMIN")

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
        // on utilise strict origins ici ; pour wildcard, passe à setAllowedOriginPatterns(...)
        cfg.setAllowedOrigins(Arrays.asList(allowedOrigins));
        cfg.setAllowedMethods(List.of(
                "GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource src =
                new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}




/**
package com.ieolympicstickets.backend.config;

import com.ieolympicstickets.backend.security.JwtService;
import com.ieolympicstickets.backend.service.UserService;
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
import com.ieolympicstickets.backend.security.JwtAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    // Has Bcrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager with UserService
    @Bean
    public AuthenticationManager authenticationManager(
            UserService userService,
            PasswordEncoder  passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    //Filter JWT, validate + put auth into this context
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtService jwtService

    ) {
        return new JwtAuthenticationFilter(jwtService);
    }

    //Spring Security filters
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                //401s
                .exceptionHandling( ex->ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        //non-authenticated
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                        //user authenticated
                        .requestMatchers("/api/auth/me").authenticated()
                        //public reads
                        .requestMatchers(HttpMethod.GET,   "/api/offers/**").permitAll()
                        .requestMatchers(HttpMethod.GET,   "/api/events/**").permitAll()
                        //Admin rights for events
                        .requestMatchers(HttpMethod.POST,   "/api/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/api/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasRole("ADMIN")
                        //Admin rights for Offers
                        .requestMatchers(HttpMethod.POST,   "/api/offers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/offers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/api/offers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/offers/**").hasRole("ADMIN")
                        //Cart validation for atuhenticated users
                        .requestMatchers(HttpMethod.POST,"/api/cart/validate").authenticated()
                        //actuator, just in case
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        //admin console
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        //rest
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class );
        return http.build();
    }

    //  Configuration CORS React / Expo
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", config);
        return src;
    }
}
*/