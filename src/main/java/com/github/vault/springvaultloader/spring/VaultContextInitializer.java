package com.github.vault.springvaultloader.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.vault.springvaultloader.http.VaultClient;
import com.github.vault.springvaultloader.model.VaultConfiguration;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.*;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.github.vault.springvaultloader.util.JsonPropertyMapper.mapFromJson;
import static com.github.vault.springvaultloader.util.VaultFileUtil.readJsonFile;
import static java.lang.System.getenv;
import static java.util.Objects.nonNull;


@Log4j2
public class VaultContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final ObjectMapper mapper = new ObjectMapper();
    public static final String ROLE_ID = "ROLE_ID";
    public static final String SECRET_ID = "SECRET_ID";
    public static final String VAULT_AUTH_PATH = "auth.json";
    public static final String VAULT_CONFIG_PATH = "vault-config.json";
    public static final String VAULT_SECRETS_PATH = "vault-secrets.json";
    public static final String ENV = "ENV";
    public static final String APP_ROLE = "APP_ROLE";

    private VaultConfiguration config;

    public VaultContextInitializer(String namespace, String appId, boolean isSync) {
        super();
        this.config = VaultConfiguration.builder()
                .sync(isSync)
                .nameSpace(namespace)
                .appId(appId)
                .build();
    }

    @Override
    public void initialize(ConfigurableApplicationContext context) {

        try {

            loadVault();
            loadContextPropertySources(context);

        } catch (Exception e) {
            log.error("Failed to initialize Vault.", e);
            throw new RuntimeException(e);
        }
    }


    private void loadContextPropertySources(ConfigurableApplicationContext context) throws IOException {

        Resource secrets = context.getResource(VAULT_SECRETS_PATH);
        List<MapPropertySource> propertySources = mapFromJson(secrets);

        propertySources.stream()
                .peek(prop -> log.info("Loaded property. [property]: {}", prop.getSource()))
                .forEach(prop -> addContextProp(context, prop));
    }

    private void addContextProp(ConfigurableApplicationContext context, MapPropertySource prop) {
        context.getEnvironment()
                .getPropertySources()
                .addFirst(prop);
    }

    private void loadVault() throws Exception {

        config = config.toBuilder()
                .env(getEnvProp(ENV))
                .roleId(getEnvProp(ROLE_ID))
                .appRole(getEnvProp(APP_ROLE))
                .secretId(getEnvProp(SECRET_ID))
                .build();

        VaultClient client = new VaultClient(config);
        VaultLoader loader = new VaultLoader(client);

        if (config.isSync()) {
            loader.writeSecrets();
        }

        loader.loadSecrets();
    }

    private String getEnvProp(String propName) {
        return getenv().get(propName);
    }
}