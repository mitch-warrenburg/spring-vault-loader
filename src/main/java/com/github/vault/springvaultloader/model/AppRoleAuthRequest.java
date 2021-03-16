package com.github.vault.springvaultloader.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppRoleAuthRequest {

    @JsonProperty("role_id")
    String roleId;

    @JsonProperty("secret_id")
    String secretId;
}