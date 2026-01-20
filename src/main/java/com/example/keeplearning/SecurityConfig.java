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
                        // Wichtig: requestMatchers werden in Reihenfolge ausgewertet.
                        // Wenn ein Pfad mehrfach gematcht wird, gewinnt der erste Treffer.
                        // Verified ist laut Projekt-Notizen "für später" – Features sollen auch ohne Verifizierung nutzbar sein.
                        .requestMatchers("/anzeigen/neu").hasRole("TEACHER")
                        .requestMatchers("/anzeigen/**").authenticated()
                        .requestMatchers("/buchung/**").hasRole("STUDENT")
                        .requestMatchers("/profil/**").authenticated()
                        .requestMatchers("/lehrer/**").hasRole("TEACHER")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/favoriten/**").hasRole("STUDENT")
                        .requestMatchers("/rechnungen/**").hasRole("STUDENT")
                        .requestMatchers("/einnahmen/**").hasRole("TEACHER")
                        .requestMatchers("/gruppen/**").authenticated()
                        .requestMatchers("/meldungen/**").authenticated()
                        .requestMatchers("/user/**").authenticated()
                        .requestMatchers("/chat/**").authenticated()
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
