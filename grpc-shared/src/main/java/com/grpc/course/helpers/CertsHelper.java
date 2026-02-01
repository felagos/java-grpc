package com.grpc.course.helpers;

import java.nio.file.Path;
import java.security.KeyStore;
import java.util.concurrent.Callable;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.GrpcSslContexts;

public class CertsHelper {

    private static CertsHelper INSTANCE = null;

    private Path KEY_STORE_PATH = Path.of("src/main/resources/keystore.jks");
    private Path TRUST_STORE_PATH = Path.of("src/main/resources/truststore.jks");
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
            var keyStore = KeyStore.getInstance(KEY_STORE_PATH.toFile(), PASSWORD);

            kmf.init(keyStore, PASSWORD);

            return kmf;
        });
    }

    public TrustManagerFactory getTrustManagerFactory() throws Exception {
        return handleException(() -> {
            var tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            var trustStore = KeyStore.getInstance(TRUST_STORE_PATH.toFile(), PASSWORD);

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

    public Path getKeyPath() {
        return KEY_STORE_PATH;
    }

    public Path getTrustPath() {
        return TRUST_STORE_PATH;
    }

}
