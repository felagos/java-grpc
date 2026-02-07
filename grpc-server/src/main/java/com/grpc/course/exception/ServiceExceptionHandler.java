package com.grpc.course.exception;

import io.grpc.Status;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class ServiceExceptionHandler {

    @GrpcExceptionHandler({ RequestValidationException.class })
    public Status handleRequestValidationException(RequestValidationException ex) {
        return Status.INVALID_ARGUMENT.withDescription(ex.getMessage());
    }

}
