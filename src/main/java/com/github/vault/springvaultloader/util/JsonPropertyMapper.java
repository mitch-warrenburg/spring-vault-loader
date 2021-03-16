package com.github.vault.springvaultloader.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.*;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@SuppressWarnings({"unchecked", "rawtypes", "OptionalUsedAsFieldOrParameterType"})
public class JsonPropertyMapper {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static List<MapPropertySource> convertEntrySet(Set<Map.Entry> entrySet, Optional<String> parentKey) {

        return entrySet.stream()
                .map((Map.Entry e) -> convertToPropertySourceList(e, parentKey))
                .flatMap(Collection::stream)
                .collect(toList());
    }

    private static List<MapPropertySource> convertToPropertySourceList(Map.Entry entry, Optional<String> parentKey) {

        String key = parentKey
                .map(parent -> parent.concat("."))
                .orElse("")
                .concat(entry.getKey().toString());

        return covertToPropertySourceList(key, entry.getValue());
    }

    private static List<MapPropertySource> covertToPropertySourceList(String key, Object value) {

        if (value instanceof LinkedHashMap)
            return convertEntrySet(((LinkedHashMap) value).entrySet(), ofNullable(key));

        return singletonList(new MapPropertySource(key, singletonMap(key, value)));
    }


    public static List<MapPropertySource> mapFromJson(Resource resource) throws IOException {

        Map propertyMap = mapper.readValue(resource.getInputStream(), Map.class);
        return convertEntrySet(propertyMap.entrySet(), Optional.empty());
    }


    public static List<MapPropertySource> mapFromJson(Map secrets) {
        return convertEntrySet(secrets.entrySet(), Optional.empty());
    }
}
