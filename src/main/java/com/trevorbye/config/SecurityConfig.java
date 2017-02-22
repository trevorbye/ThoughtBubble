package com.trevorbye.config;

import com.trevorbye.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userProfileService);
        auth.authenticationProvider(authenticationProvider());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userProfileService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/css/**", "/javascript/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http    .httpBasic()
                .and()
                    .rememberMe()
                    .key("login-remember-me")
                    .alwaysRemember(true)
                    .rememberMeCookieName("thought-bubble-login")
                    //ten day token validity
                    .tokenValiditySeconds(864000)
                .and()
                    .csrf().disable()
                    .authorizeRequests()
                    .antMatchers("/", "/index.html", "/home.html", "/getLatestPost",
                            "/application-socket-conn/**", "/login", "/login.html", "/user", "/register.html",
                            "/register-user", "/register").permitAll()
                    .anyRequest().authenticated();
    }
}
