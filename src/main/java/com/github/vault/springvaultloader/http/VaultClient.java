package com.github.vault.springvaultloader.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.vault.springvaultloader.model.AppRoleAuthRequest;
import com.github.vault.springvaultloader.model.AppRoleAuthResponse;
import com.github.vault.springvaultloader.model.SecretResponse;
import com.github.vault.springvaultloader.model.VaultConfiguration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Date;

import static com.github.vault.springvaultloader.http.RestTemplateInstance.initClient;
import static com.github.vault.springvaultloader.spring.VaultContextInitializer.VAULT_AUTH_PATH;
import static com.github.vault.springvaultloader.spring.VaultContextInitializer.VAULT_SECRETS_PATH;
import static com.github.vault.springvaultloader.util.VaultFileUtil.readJsonFile;
import static com.github.vault.springvaultloader.util.VaultFileUtil.writeJsonFile;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.StringUtils.isEmpty;

@Log4j2
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VaultClient {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String TOKEN_HEADER = "X-vault-token";
    private static final String NAMESPACE_HEADER = "x-vault-namespace";
    private static final String AUTH_PATH_TEMPLATE = "/auth/%s/login";
    private static final String SECRET_PATH_TEMPLATE = "/secret/%s/config";
    private static final String BASE_URL = ${BASE_URL};

    private String env;
    private String appId;
    private String roleId;
    private String appRole;
    private String authUrl;
    private String secretId;
    private String secretUrl;
    private String nameSpace;
    private String secretPath;
    private RestTemplate restTemplate;

    public VaultClient(VaultConfiguration vaultConfiguration) throws Exception {
        this.env = vaultConfiguration.getEnv();
        this.roleId = vaultConfiguration.getRoleId();
        this.appRole = vaultConfiguration.getAppRole();
        this.secretId = vaultConfiguration.getSecretId();
        this.nameSpace = vaultConfiguration.getNameSpace();
        this.appId = vaultConfiguration.getappId();
        this.restTemplate = initClient(vaultConfiguration.isProxy());
        this.secretPath = format("%s/%s/%s", appId, env, appRole);
        this.authUrl = BASE_URL + format(AUTH_PATH_TEMPLATE, secretPath);
        this.secretUrl = BASE_URL + format(SECRET_PATH_TEMPLATE, secretPath);
    }

    public AppRoleAuthResponse authenticate() throws Exception {

        log.info("Refreshing vault client token.");

        HttpEntity<String> request = new HttpEntity<>(buildRequestPayload(), buildHttpHeaders());
        String responseJson = requireNonNull(restTemplate.postForObject(authUrl, request, String.class));

        AppRoleAuthResponse response = mapper.readValue(responseJson, AppRoleAuthResponse.class);

        Date expiry = Date.from(now().plusSeconds(response.getAuth().getLeaseDuration())
                .atZone(systemDefault()).toInstant());

        writeJsonFile(response.withExpiry(expiry), VAULT_AUTH_PATH);

        return response;
    }

    public SecretResponse read(String authToken) throws Exception {

        RequestEntity<?> requestEntity = new RequestEntity<>(buildHttpHeaders(authToken), HttpMethod.GET, new URI(secretUrl));

        String responseJson = requireNonNull(restTemplate.exchange(requestEntity, String.class).getBody());
        SecretResponse secretResponse = mapper.readValue(responseJson, SecretResponse.class);

        writeJsonFile(secretResponse.getData(), VAULT_SECRETS_PATH);

        return secretResponse;
    }

    public void write(String authToken) throws IOException {

        String secrets = readJsonFile(VAULT_SECRETS_PATH);

        if (isEmpty(secrets))
            return;

        HttpEntity<String> request = new HttpEntity<>(readJsonFile(VAULT_SECRETS_PATH), buildHttpHeaders(authToken));
        restTemplate.postForObject(secretUrl, request, String.class);
    }

    private HttpHeaders buildHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(NAMESPACE_HEADER, nameSpace);
        headers.add(CONTENT_TYPE, APPLICATION_JSON_VALUE);
        return headers;
    }

    private HttpHeaders buildHttpHeaders(String authToken) {
        HttpHeaders headers = buildHttpHeaders();
        if (nonNull(authToken)) {
            headers.add(TOKEN_HEADER, authToken);
        }
        return headers;
    }

    String buildRequestPayload() throws Exception {
        return mapper.writeValueAsString(new AppRoleAuthRequest(roleId, secretId));
    }
}