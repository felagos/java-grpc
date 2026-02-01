package com.grpc.course.helpers;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.concurrent.Callable;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.GrpcSslContexts;

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

    public KeyManagerFactory getKeyManagerFactory() throws Exception {
        return handleException(() -> {
            var kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            var keyStore = KeyStore.getInstance("JKS");
            
            try (InputStream ksStream = getClass().getClassLoader().getResourceAsStream(KEY_STORE_NAME)) {
                if (ksStream == null) {
                    throw new Exception("Keystore file not found in classpath: " + KEY_STORE_NAME);
                }
                keyStore.load(ksStream, PASSWORD);
            }

            kmf.init(keyStore, PASSWORD);

            return kmf;
        });
    }

    public TrustManagerFactory getTrustManagerFactory() throws Exception {
        return handleException(() -> {
            var tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            var trustStore = KeyStore.getInstance("JKS");
            
            try (InputStream tsStream = getClass().getClassLoader().getResourceAsStream(TRUST_STORE_NAME)) {
                if (tsStream == null) {
                    throw new Exception("Truststore file not found in classpath: " + TRUST_STORE_NAME);
                }
                trustStore.load(tsStream, PASSWORD);
            }

            tmf.init(trustStore);

            return tmf;
        });
    }

    public SslContext serverSslContext() throws Exception {
        return handleException(() -> {
            var kmf = getKeyManagerFactory();
            var sslBuilder = SslContextBuilder.forServer(kmf);

            return GrpcSslContexts.configure(sslBuilder).build();
        });
    }

    public SslContext clientSslContext() throws Exception {
        return handleException(() -> {
            var tmf = getTrustManagerFactory();
            var sslBuilder = SslContextBuilder.forClient().trustManager(tmf);

            return GrpcSslContexts.configure(sslBuilder).build();
        });
    }

    private <T> T handleException(Callable<T> callable) throws Exception {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new Exception("Error initializing KeyManagerFactory", e);
        }
    }

}
