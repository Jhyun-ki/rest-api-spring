package com.hyunki.restapi.configs;

import com.hyunki.restapi.accounts.AccountRepository;
import com.hyunki.restapi.accounts.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AccountService accountService;
    private final AccountRepository accountRepository;

    @Bean
    TokenStore tokenStore() {
        return new InMemoryTokenStore();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

//    @Bean
//    public PasswordEncoder getPasswordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

//    @Bean
//    public UserDetailsService userDetailsService(AccountRepository accountRepository, PasswordEncoder passwordEncoder ) {
//        return new AccountService(accountRepository, passwordEncoder);
//    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> {
            web.ignoring().mvcMatchers("/docs/index.html");
            web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
        };
    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.anonymous()
//                .and()
//                .formLogin()
//                .and()
//                .authorizeRequests()
//                .mvcMatchers(HttpMethod.GET, "/api/**").authenticated()
//                .anyRequest().authenticated();
//
//        return http.build();
//    }
}
