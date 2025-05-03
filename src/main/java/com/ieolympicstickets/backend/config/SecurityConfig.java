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