package com.github.vault.springvaultloader.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.vault.springvaultloader.http.VaultClient;
import com.github.vault.springvaultloader.model.AppRoleAuthResponse;
import com.github.vault.springvaultloader.retry.RetryableWrapper;
import lombok.extern.log4j.Log4j2;

import static com.github.vault.springvaultloader.spring.VaultContextInitializer.VAULT_AUTH_PATH;
import static com.github.vault.springvaultloader.util.VaultFileUtil.doesFileExist;
import static com.github.vault.springvaultloader.util.VaultFileUtil.readJsonFile;
import static java.time.LocalDateTime.now;
import static java.time.ZoneId.systemDefault;

@Log4j2
public class VaultLoader {

    private static final ObjectMapper mapper = new ObjectMapper();

    private VaultClient client;
    private RetryableWrapper retryable = new RetryableWrapper();

    public VaultLoader(VaultClient client) {
        this.client = client;
    }

    public void loadSecrets() throws Exception {

        String authToken = retryable.withRetry(() -> fetchClientToken());

        log.info("Using client token: {}", authToken);

        client.read(authToken);
    }

    public void writeSecrets() throws Exception {
        client.write(fetchClientToken());
    }

    private String fetchClientToken() throws Exception {

        try {

            boolean hasAuthFile = doesFileExist(VAULT_AUTH_PATH);

            if (!hasAuthFile) {
                return client.authenticate().getAuth().getClientToken();
            }

            AppRoleAuthResponse response = mapper.readValue(readJsonFile(VAULT_AUTH_PATH), AppRoleAuthResponse.class);

            boolean shouldRefresh = response.getExpiry()
                    .toInstant()
                    .atZone(systemDefault())
                    .toLocalDateTime()
                    .isBefore(now());

            if (shouldRefresh) {
                return client.authenticate().getAuth().getClientToken();
            }

            return response.getAuth().getClientToken();

        } catch (Exception exception) {
            return client.authenticate().getAuth().getClientToken();
        }
    }
}
