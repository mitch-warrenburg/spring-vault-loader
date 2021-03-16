### Spring Vault Loader

A lightweight library for syncing up with HashiCorp Vault using appId.

#### Usage:

Add the VaultContextInitializer

```java
...
import com.github.vault.springvaultloader.spring.VaultContextInitializer;

@SpringBootApplication
public class MySecureApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(HydraApplication.class)
        .initializers(new VaultContextInitializer("myNamespace", "myAppId", false))
        .run(args);
  }
}
```

Declare the following environment variables:

```shell script
ENV=nonprod
APP_ROLE=myApp
ROLE_ID=00000000-0000-0000-0000-000000000000
SECRET_ID=00000000-0000-0000-0000-000000000000
```

Add spring-vault-loader dependency:

```groovy
dependencies {
    implementation 'com.github.vault:spring-vault-loader:1.043'
}
```

![](https://media.giphy.com/media/xT8qAY7e9If38xkrIY/source.gif)

PARTY!!!! 🎉🎉🎉
