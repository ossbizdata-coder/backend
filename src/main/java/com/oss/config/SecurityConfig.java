package com.oss.config;
import com.oss.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.AccessDeniedHandler;
import jakarta.servlet.http.HttpServletResponse;
@Configuration
public class SecurityConfig {
    @Autowired
    private JwtAuthFilter jwtAuthFilter;
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            String username = request.getRemoteUser();
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String auth = request.getHeader("Authorization");

            System.err.println("========== ACCESS DENIED ==========");
            System.err.println("Method: " + method);
            System.err.println("URI: " + uri);
            System.err.println("User: " + username);
            System.err.println("Has Auth Header: " + (auth != null));
            System.err.println("Exception: " + accessDeniedException.getMessage());
            System.err.println("===================================");

            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: " + accessDeniedException.getMessage());
        };
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                                           session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                                  )
                .authorizeHttpRequests(auth -> auth
                    // Public endpoints - Auth & FoodHut Items
                    .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/items", "/api/items/**", "/items", "/items/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/items", "/api/items/**", "/items", "/items/**").permitAll()
                    // All other /api/** endpoints - accessible by STAFF, ADMIN, SUPERADMIN
                    .requestMatchers("/api/**").hasAnyRole("STAFF", "ADMIN", "SUPERADMIN")
                    // Default - require authentication
                    .anyRequest().authenticated()
                )
                .exceptionHandling(eh -> eh.accessDeniedHandler(accessDeniedHandler()))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}