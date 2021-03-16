package com.github.vault.springvaultloader.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import java.util.Date;
import java.util.List;

@Data
@Wither
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppRoleAuthResponse {

    private Date expiry;

    @JsonProperty("lease_id")
    private String leaseId;

    @JsonProperty("wrap_info")
    private String wrapInfo;

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("lease_duration")
    private long leaseDuration;

    private String data;
    private String warnings;
    private boolean renewable;
    private Auth auth;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Auth {

        @JsonProperty("client_token")
        private String clientToken;

        @JsonProperty("entity_id")
        private String entityId;

        @JsonProperty("auth_token")
        private String authToken;

        @JsonProperty("lease_duration")
        private long leaseDuration;

        @JsonProperty("token_type")
        private String tokenType;

        @JsonProperty("token_policies")
        private List<String> tokenPolicies;

        private String accessor;
        private boolean orphan;
        private boolean renewable;
        private List<String> policies;
        private Metadata metadata;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class Metadata {

        @JsonProperty("role_name")
        private String roleName;
    }
}
