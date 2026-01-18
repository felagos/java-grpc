package com.grpc.course.interceptor;

import build.buf.protovalidate.ValidationResult;
import build.buf.protovalidate.Validator;
import build.buf.protovalidate.exceptions.ValidationException;
import com.google.protobuf.Message;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationInterceptor implements ServerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ValidationInterceptor.class);
    private final Validator validator;

    public ValidationInterceptor(Validator validator) {
        this.validator = validator;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // Crear el listener del siguiente interceptor una sola vez
        final ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);

        return new ServerCall.Listener<ReqT>() {
            @Override
            public void onMessage(ReqT message) {
                // Validar si es un Message de protobuf
                if (message instanceof Message) {
                    try {
                        ValidationResult result = validator.validate((Message) message);
                        if (!result.isSuccess()) {
                            logger.warn("Validation failed for message: {}", result.getViolations());
                            call.close(Status.INVALID_ARGUMENT
                                    .withDescription("Validation failed: " + result.getViolations()),
                                    new Metadata());
                            return;
                        }
                    } catch (ValidationException e) {
                        logger.error("Validation exception: {}", e.getMessage());
                        call.close(Status.INVALID_ARGUMENT
                                .withDescription("Validation error: " + e.getMessage()),
                                new Metadata());
                        return;
                    }
                }

                // Delegar al siguiente interceptor si la validación fue exitosa
                delegate.onMessage(message);
            }

            @Override
            public void onHalfClose() {
                delegate.onHalfClose();
            }

            @Override
            public void onCancel() {
                delegate.onCancel();
            }

            @Override
            public void onComplete() {
                delegate.onComplete();
            }
        };
    }
}
