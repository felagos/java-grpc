package com.grpc.course.common;

import io.grpc.Context;

public class GrpcContextKeys {

    public static final Context.Key<String> USER_CONTEXT_KEY = Context.key("user");

    private GrpcContextKeys() {
    }
}
