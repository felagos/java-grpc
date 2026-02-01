package com.grpc.course.common;

import java.nio.file.Path;

public class CertsHelper {

    public static Path KEY_STORE_PATH = Path.of("src/main/resources/keystore.jks");
    public static Path TRUST_STORE_PATH = Path.of("src/main/resources/truststore.jks");

}
