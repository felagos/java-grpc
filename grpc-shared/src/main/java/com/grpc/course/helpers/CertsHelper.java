package com.grpc.course.helpers;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.concurrent.Callable;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * CertsHelper - DEPRECATED: SSL/TLS is no longer used in this project.
 * This class is kept for backward compatibility only.
 * All gRPC communication now uses plaintext.
 */
@Deprecated
public class CertsHelper {

    private static CertsHelper INSTANCE = null;

    private String KEY_STORE_NAME = "keystore.jks";
    private String TRUST_STORE_NAME = "truststore.jks";
    private char[] PASSWORD = "changeit".toCharArray();

    private CertsHelper() {
    }

    public static CertsHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CertsHelper();
        }
        return INSTANCE;
    }

    @Deprecated
    public KeyManagerFactory getKeyManagerFactory() throws Exception {
        throw new UnsupportedOperationException("SSL/TLS is no longer used. Use plaintext gRPC communication instead.");
    }

    @Deprecated
    public TrustManagerFactory getTrustManagerFactory() throws Exception {
        throw new UnsupportedOperationException("SSL/TLS is no longer used. Use plaintext gRPC communication instead.");
    }

    @Deprecated
    public Object serverSslContext() throws Exception {
        throw new UnsupportedOperationException("SSL/TLS is no longer used. Use plaintext gRPC communication instead.");
    }

    @Deprecated
    public Object clientSslContext() throws Exception {
        throw new UnsupportedOperationException("SSL/TLS is no longer used. Use plaintext gRPC communication instead.");
    }

}
