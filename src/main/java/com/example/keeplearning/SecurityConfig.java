package com.example.keeplearning;

import com.example.keeplearning.security.CustomAuthenticationFailureHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private CustomAuthenticationFailureHandler failureHandler;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/anzeigen/neu").hasAuthority("VERIFIED")
                        .requestMatchers("/anzeigen/neu").hasRole("TEACHER")
                        .requestMatchers("/anzeigen/**").authenticated()
                        .requestMatchers("/buchung/**").hasAuthority("VERIFIED")
                        .requestMatchers("/buchung/**").hasRole("STUDENT")
                        .requestMatchers("/profil/**").authenticated()
                        .requestMatchers("/lehrer/**").hasAuthority("VERIFIED")
                        .requestMatchers("/lehrer/**").hasRole("TEACHER")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/favoriten/**").hasRole("STUDENT")
                        .requestMatchers("/user/**").authenticated()
                        .requestMatchers("/chat/**").authenticated()
                        .requestMatchers("/share/**").authenticated()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home")
                        .failureHandler(failureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/home")
                        .invalidateHttpSession(true)
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
