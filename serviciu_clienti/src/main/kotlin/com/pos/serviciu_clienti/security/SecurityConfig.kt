package com.pos.serviciu_clienti.security

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SecurityConfig {

    @Bean
    fun jwtFilterRegistration(filter: JwtAuthenticationFilter): FilterRegistrationBean<JwtAuthenticationFilter> {
        return FilterRegistrationBean<JwtAuthenticationFilter>().apply {
            setFilter(filter)
            addUrlPatterns("/api/*")
            order = 1
        }
    }
}
