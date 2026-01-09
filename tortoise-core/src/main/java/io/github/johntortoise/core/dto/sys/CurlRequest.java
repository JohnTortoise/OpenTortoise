package io.github.johntortoise.core.dto.sys;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CurlRequest {
    private String method = "GET";
    private String url;
    private Map<String, String> headers = new LinkedHashMap<>();
    private String requestBody;
    private Map<String, String> queryParams = new HashMap<>();


}