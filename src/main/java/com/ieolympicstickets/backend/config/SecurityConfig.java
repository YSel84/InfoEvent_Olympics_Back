package com.ieolympicstickets.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    //Admin credentials
    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${FRONT_USERNAME}")
    private String frontUsername;
    @Value("${FRONT_PASSWORD}")
    private String frontPassword;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder) {
        UserDetails admin = User.builder().username(adminUsername).password(encoder.encode(adminPassword)).roles("ADMIN").build();
        UserDetails front = User.builder().username(frontUsername).password(encoder.encode(frontPassword)).roles("FRONT").build();
        return new InMemoryUserDetailsManager(admin, front);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        //Allowed origins
        cfg.setAllowedOrigins(List.of("http://localhost:8081","https://infoeventolympics.netlify.app"));
        //Allowed methods
        cfg.addAllowedMethod("GET");
        cfg.addAllowedMethod("POST");
        cfg.addAllowedMethod("PUT");
        cfg.addAllowedMethod("PATCH");
        cfg.addAllowedMethod("DELETE");
        cfg.addAllowedMethod("OPTIONS");
        //Allowed headers
        cfg.addAllowedHeader("*");
        //Allow credentials
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;

    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //Cors activated:
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable) // désactive CSRF pour les requêtes POST simples
                .authorizeHttpRequests(auth->auth
                        //pre-flight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        //requests by role
                        .requestMatchers(HttpMethod.GET, "/api/events/**").hasAnyRole("ADMIN","FRONT")
                        .requestMatchers(HttpMethod.POST, "/api/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasRole("ADMIn")
                        .requestMatchers(HttpMethod.PUT, "/api/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasRole("ADMIN")
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().permitAll() // tout est public
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}