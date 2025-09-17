package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
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

    /** セキュリティ対象外設定（静的リソースなど） */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers(
                        "/webjars/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/h2-console/**",
                        "/favicon.ico",
                        "/.well-known/**"
                );
    }

    /** セキュリティ設定 */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // ログイン・サインアップは誰でもアクセス可能
                .requestMatchers(
                        "/login",
                        "/kintai/signup",
                        "/kintai/signup/rest"
                ).permitAll()
                // 管理者専用ページ
                .requestMatchers("/admin").hasAuthority("ROLE_ADMIN")
                // Ajax 用 approve/reject/apply は認証必須だが、セッションで JSON が返る
                .requestMatchers(
                        "/kintai/koutsuhi/apply",
                        "/kintai/koutsuhi/approve",
                        "/kintai/koutsuhi/reject"
                ).authenticated()
                // API は静的なものや外部呼び出し用
                .requestMatchers("/kintai/api/**").permitAll()
                // それ以外は認証必須
                .requestMatchers("/kintai/**").authenticated()
                .anyRequest().authenticated()
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
            // CSRF無効（Ajax 用）
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /** パスワードエンコーダ設定 */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** 認証マネージャ設定 */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(userDetailsService)
                   .passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }

    /** ログイン成功後の遷移先をロールで分ける */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }
}
