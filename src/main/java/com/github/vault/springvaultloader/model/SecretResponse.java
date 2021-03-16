package com.github.vault.springvaultloader.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;

import static com.github.vault.springvaultloader.util.JsonPropertyMapper.mapFromJson;
import static java.util.stream.Collectors.toMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecretResponse {

    @JsonProperty("lease_id")
    private String leaseId;

    @JsonProperty("wrap_info")
    private String wrapInfo;

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("lease_duration")
    private long leaseDuration;

    private String auth;
    private String warnings;
    private boolean renewable;
    private Map<String, Object> data;

    @JsonSetter
    public void setData(Map<String, Object> secretData) {

        this.data = mapFromJson(secretData).stream()
                .map(source -> source.getSource().entrySet())
                .flatMap(Collection::stream)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}