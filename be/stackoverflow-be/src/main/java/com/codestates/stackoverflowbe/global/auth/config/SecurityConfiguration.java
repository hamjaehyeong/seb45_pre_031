package com.codestates.stackoverflowbe.global.auth.config;

import com.codestates.stackoverflowbe.domain.account.service.AccountService;
import com.codestates.stackoverflowbe.global.auth.filter.JwtVerificationFilter;
import com.codestates.stackoverflowbe.global.auth.handler.AccountAuthenticationSuccessHandler;
import com.codestates.stackoverflowbe.global.auth.handler.UserAccessDeniedHandler;
import com.codestates.stackoverflowbe.global.auth.filter.JwtAuthenticationFilter;
import com.codestates.stackoverflowbe.global.auth.handler.UserAuthenticationEntryPoint;
import com.codestates.stackoverflowbe.global.auth.handler.UserAuthenticationFailureHandler;
import com.codestates.stackoverflowbe.global.auth.jwt.JwtTokenizer;
//import com.codestates.stackoverflowbe.global.auth.handler.OAuth2UserSuccessHandler;
import com.codestates.stackoverflowbe.global.auth.utils.CustomAuthorityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class SecurityConfiguration {

    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;
    private final AccountService accountService;
//    private final CorsFilter corsFilter;

    @Lazy // accountService의 순환참조 문제 해결
    public SecurityConfiguration(JwtTokenizer jwtTokenizer, CustomAuthorityUtils authorityUtils, AccountService accountService
//                                 CorsFilter corsFilter
    ) {
        this.jwtTokenizer = jwtTokenizer;
        this.authorityUtils = authorityUtils;
        this.accountService = accountService;
//        this.corsFilter = corsFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .headers().frameOptions().sameOrigin() // (해당 옵션 유효한 경우 h2사용가능) SOP 정책 유지, 다른 도메인에서 iframe 로드 방지
                .and()
                .csrf().disable()
                .cors(Customizer.withDefaults()) //CORS 처리하는 가장 쉬운 방법인 CorsFilter 사용, CorsConfigurationSource Bean을 제공
                .formLogin().disable() // CSR 방식을 사용하기 때문에 formLogin 방식 사용하지 않음
                .httpBasic().disable() // UsernamePasswordAuthenticationFilter, BasicAuthenticationFilter 등 비활성화
                .exceptionHandling() // 예외처리 기능
                .authenticationEntryPoint(new UserAuthenticationEntryPoint()) // 인증 실패시 처리 (UserAuthenticationEntryPoint 동작)
                .accessDeniedHandler(new UserAccessDeniedHandler()) //인가 거부시 UserAccessDeniedHandler가 처리되도록 설계
                .and()
                .apply(new CustomFilterConfigurer()) // 커스터마이징한 필터 추가
                .and() // 허용되는 HttpMethod와 역할 설정
                .authorizeHttpRequests( authorize -> authorize
                        .antMatchers(HttpMethod.POST, "/accounts/**").permitAll()
                        .antMatchers(HttpMethod.GET, "/accounts").hasRole("ADMIN")
//                .oauth2Login( oauth2 -> oauth2
//                        //OAuth2 인증이 성공했을 때 핸들러 처리
//                        .successHandler(new OAuth2UserSuccessHandler(jwtTokenizer, authorityUtils, accountService)) //OAuth 2.0 로그인이 성공했을 때의 동작을 정의하는 커스텀 핸들러
                );

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        //모든 출처(Origin)에 대해 스크립트 기반 HTTP 통신 허용
        configuration.setAllowedOrigins(Arrays.asList("*"));

        //4가지 HTTP Method에 대한 HTTP 통신 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "DELETE"));

        // CorsConfigurationSource 인터페이스 구현 클래스인 UrlBasedCorsConfigurationSource 클래스 객체 생성
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // 모든 URL에 상기 CORS 정책 적용
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    public class CustomFilterConfigurer extends AbstractHttpConfigurer<CustomFilterConfigurer, HttpSecurity> {

        @Override
        public void configure(HttpSecurity builder) throws Exception {
            // authenticationManager : 사용자가 로그인 요청시 입력한 아이디와 패스워드를 해당 객체로 전달하여 인증 수행하며, 결과에 따라 로직 처리
            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class); // AuthenticationManager 객체얻기

            JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtTokenizer); // JwtAuthenticationFilter 객체 생성하며 DI하기

            // AbstractAuthenticationProcessingFilter에서 상속받은 filterProcessurl을 설정 (설정하지 않으면 default 값인 /Login)
            jwtAuthenticationFilter.setFilterProcessesUrl("/accounts/login");
            jwtAuthenticationFilter.setAuthenticationSuccessHandler(new AccountAuthenticationSuccessHandler());
            jwtAuthenticationFilter.setAuthenticationFailureHandler(new UserAuthenticationFailureHandler());

            JwtVerificationFilter jwtVerificationFilter = new JwtVerificationFilter(jwtTokenizer, authorityUtils);

            // Spring Security FilterChain에 추가
            builder
//                    .addFilter(corsFilter)
                    .addFilter(jwtAuthenticationFilter)
                    // OAuth2LoginAuthenticationFilter : OAuth2.0 권한 부여 응답 처리 클래스 뒤에 jwtVerificationFilter 추가 (Oauth
                    .addFilterAfter(jwtVerificationFilter, OAuth2LoginAuthenticationFilter.class);
        }
    }



}