package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@EnableWebSecurity
@Configuration
@Lazy
public class SecurityConfig {

    @Autowired
    @Lazy
    private UserDetailsService userDetailsService;

    /** セキュリティ設定 */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        .authorizeHttpRequests(authorize -> authorize
            // ★ ログイン・サインアップ + 静的リソースは許可
            .requestMatchers(
                    "/login",
                    "/kintai/signup",
                    "/kintai/signup/rest",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/webjars/**",
                    "/favicon.ico"
            ).permitAll()

            // ★ 管理者専用
            .requestMatchers("/admin").hasAuthority("ROLE_ADMIN")

            // ★ kintai全部保護（API含む）
            .requestMatchers("/kintai**").authenticated()
            .requestMatchers("/h2-console/**").permitAll()
            // ★ その他も全部認証必須
            .anyRequest().authenticated()
        )
        .headers(headers -> headers
        	    .frameOptions(frame -> frame.disable()) // ★これ追加！！！
        	)
        .formLogin(form -> form
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .usernameParameter("userId")
            .passwordParameter("password")
            .successHandler(authenticationSuccessHandler())
            .failureUrl("/login?error")
            .permitAll()
        )
        .logout(logout -> logout
            .logoutSuccessUrl("/login?logout")
            .permitAll()
        )
        // ★ CSRF無効（Ajax用）
        .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /** パスワードエンコーダ */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** 認証マネージャ */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(userDetailsService)
                   .passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }

    /** ログイン成功後の遷移制御 */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }
}