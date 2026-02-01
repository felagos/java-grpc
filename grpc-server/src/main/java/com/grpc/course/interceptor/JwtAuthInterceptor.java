package com.grpc.course.interceptor;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.grpc.course.common.GrpcContextKeys;

public class JwtAuthInterceptor implements ServerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthInterceptor.class);
    private static final Metadata.Key<String> JWT_KEY = Metadata.Key.of("authorization",
            Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String methodName = call.getMethodDescriptor().getFullMethodName();
        logger.info("JWT Interceptor called for method: {}", methodName);

        if (methodName.contains("Withdraw") || methodName.endsWith("/Withdraw")) {
            logger.info("JWT validation required for method: {}", methodName);

            String authHeader = headers.get(JWT_KEY);

            if (authHeader == null || authHeader.trim().isEmpty()) {
                logger.warn("Missing authorization header for withdraw request");
                call.close(Status.UNAUTHENTICATED
                        .withDescription("Authorization header is required for withdraw operations"),
                        new Metadata());
                return new ServerCall.Listener<ReqT>() {
                };
            }

            if (!authHeader.startsWith("Bearer ")) {
                logger.warn("Invalid authorization header format: {}", authHeader);
                call.close(Status.UNAUTHENTICATED
                        .withDescription("Authorization header must start with 'Bearer '"),
                        new Metadata());
                return new ServerCall.Listener<ReqT>() {
                };
            }

            String jwt = authHeader.substring(7);

            if (!isValidJwt(jwt)) {
                logger.warn("Invalid JWT token provided");
                call.close(Status.UNAUTHENTICATED
                        .withDescription("Invalid JWT token"),
                        new Metadata());
                return new ServerCall.Listener<ReqT>() {
                };
            }

            String username = extractUsernameFromJwt(jwt);
            logger.info("JWT validation successful for withdraw request. User: {}", username);
            
            Context context = Context.current().withValue(GrpcContextKeys.USER_CONTEXT_KEY, username);
            return Contexts.interceptCall(context, call, headers, next);
        } else {
            logger.debug("Skipping JWT validation for method: {}", methodName);
        }

        return next.startCall(call, headers);
    }

    private boolean isValidJwt(String jwt) {
        if (jwt == null || jwt.trim().isEmpty()) {
            return false;
        }

        boolean isValid = jwt.startsWith("valid");

        logger.debug("JWT validation result: {} for token: {}", isValid,
                jwt.length() > 10 ? jwt.substring(0, 10) + "..." : jwt);

        return isValid;
    }

    private String extractUsernameFromJwt(String jwt) {
        // En un caso real, aquí se decodificaría el JWT y se extraería el claim del usuario
        // Por simplicidad, extraemos el usuario del token (formato: "valid-username")
        if (jwt != null && jwt.contains("-")) {
            String username = jwt.substring(jwt.indexOf("-") + 1);
            logger.debug("Extracted username: {} from JWT", username);
            return username;
        }
        return "unknown";
    }
}