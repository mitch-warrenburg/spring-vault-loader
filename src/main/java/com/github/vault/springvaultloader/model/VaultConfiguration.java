package com.github.vault.springvaultloader.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class VaultConfiguration {

    private String env;
    private String roleId;
    private String appRole;
    private String secretId;
    private String nameSpace;
    private String appId;

    @Builder.Default
    private boolean sync = false;

    @Builder.Default
    private boolean proxy = false;
}
