package com.smart.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class MyConfig {

    // ✅ Security Filter Chain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
        		 .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN") // Admin pages
                .requestMatchers("/user/**").hasRole("USER") // user page
                .requestMatchers("/api/sendemail").permitAll()  // senEmail api
                .requestMatchers("/**").permitAll()           // Public access
                .anyRequest().authenticated()                  // All other requests need authentication
            )
            .formLogin(form -> form
                    .loginPage("/signin")   // Path to your custom login page
                    .loginProcessingUrl("/dologin")
                    .defaultSuccessUrl("/user/index")
                    .failureUrl("/loginfail")
                    .permitAll()
              )            // Form-based login
            
            .logout(logout -> logout
            		.logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                    .logoutSuccessUrl("/signin?logout") // Redirect after logout
                    .invalidateHttpSession(true)      // Invalidate session on logout
                    .deleteCookies("JSESSIONID")     // Clear session cookies
                )          // Logout support
            .build();
    }

    

    
    // ✅ UserDetailsService Bean (Your Custom Implementation)
    @Bean
    public UserDetailsService getUserDetailsService() {
        return new UserDetailsServiceImpl();
    }

    // ✅ Password Encoder (BCrypt)
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ DaoAuthenticationProvider (Set UserDetails and PasswordEncoder)
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ✅ AuthenticationManager (Pass Custom authenticationProvider)
    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider provider) {
        return new ProviderManager(List.of(provider));
    }
}
