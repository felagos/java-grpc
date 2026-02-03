package com.grpc.course.config;

import build.buf.protovalidate.Validator;
import build.buf.protovalidate.ValidatorFactory;
import com.grpc.course.interceptor.JwtAuthInterceptor;
import com.grpc.course.interceptor.ValidationInterceptor;
import io.grpc.ServerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.grpc.server.GlobalServerInterceptor;

@Configuration
public class GrpcServerConfiguration {

    @Bean
    public Validator protoValidator() {
        return ValidatorFactory.newBuilder().build();
    }

    @Bean
    @GlobalServerInterceptor
    @Order(100)
    public ServerInterceptor jwtAuthInterceptor() {
        return new JwtAuthInterceptor();
    }

    @Bean
    @GlobalServerInterceptor
    @Order(200)
    public ServerInterceptor validationInterceptor(Validator validator) {
        return new ValidationInterceptor(validator);
    }
}
