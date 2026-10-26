package com.ecommers.order_service.config;


import com.ecommers.order_service.sercurity.ServiceTokenProvider;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductServiceFeignConfig {

    @Bean
    public RequestInterceptor productServiceAuthInterceptor(
            ServiceTokenProvider serviceTokenProvider) {

        return requestTemplate -> {
            String token = serviceTokenProvider.generateToken();

            requestTemplate.header(
                    "Authorization",
                    "Bearer " + token
            );
        };
    }
}

