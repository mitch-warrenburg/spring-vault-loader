package com.github.vault.springvaultloader.http;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class RestTemplateInstance {

    private static final int PROXY_PORT = 8080;
    private static final String PROXY_HOST = ${PROXY_HOST};

    public static RestTemplate initClient(boolean isProxy) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {

        DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory();
        defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        SSLContext sslContext = SSLContextBuilder
                .create()
                .setSecureRandom(new SecureRandom())
                .loadTrustMaterial(new TrustAllStrategy())
                .build();

        HttpClient client = HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext))
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .setProxy(isProxy ? new HttpHost(PROXY_HOST, PROXY_PORT) : null)
                .build();

        return new RestTemplateBuilder()
                .uriTemplateHandler(defaultUriBuilderFactory)
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(client))
                .build();
    }
}