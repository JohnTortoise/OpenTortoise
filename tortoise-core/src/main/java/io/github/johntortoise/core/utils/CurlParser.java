package io.github.johntortoise.core.utils;

import io.github.johntortoise.core.dto.sys.CurlRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CurlParser {

    private static final Pattern METHOD_PATTERN = Pattern.compile("--request\\s+([A-Z]+)|-X\\s+([A-Z]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern URL_PATTERN = Pattern.compile("'(https?://[^']+)'|\"(https?://[^\"]+)\"|\\s+(https?://\\S+)");
    private static final Pattern HEADER_PATTERN = Pattern.compile("(?:--header|-H)\\s+(?:'([^']+)'|\"([^\"]+)\")", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATA_PATTERN = Pattern.compile("(?:--data-raw|--data|--data-binary|-d)\\s+(?:'([^']*)'|\"([^\"]*)\"|(\\S+))", Pattern.CASE_INSENSITIVE);

    /**
     * 解析cURL命令字符串
     * @param curlCommand cURL命令字符串
     * @return 解析后的CurlRequest对象
     */
    public static CurlRequest parse(String curlCommand) {

        if (curlCommand == null || curlCommand.trim().isEmpty()) {
            throw new IllegalArgumentException("cURL命令不能为空");
        }

        String command = curlCommand.trim();
        if (command.startsWith("curl ")) {
            command = command.substring(4).trim();
        } else if (command.startsWith("curl")) {
            command = command.substring(4).trim();
        }

        CurlRequest request = new CurlRequest();

        parseUrl(command, request);

        parseHeaders(command, request);

        parseRequestBody(command, request);

        extractQueryParams(request);

        if(EmptyUtil.isNotEmpty(request.getRequestBody())){
            request.setMethod("POST");
        }

        return request;
    }

    /**
     * 解析HTTP方法
     */
    private static void parseMethod(String command, CurlRequest request) {
        Matcher matcher = METHOD_PATTERN.matcher(command);
        if (matcher.find()) {
            String method = matcher.group(1);
            if (method == null) {
                method = matcher.group(2);
            }
            if (method != null) {
                request.setMethod(method.toUpperCase());
            }
        }
    }

    /**
     * 解析URL
     */
    private static void parseUrl(String command, CurlRequest request) {
        Matcher matcher = URL_PATTERN.matcher(command);

        // 查找第一个匹配的URL（通常是紧跟在curl后面的）
        while (matcher.find()) {
            String url = matcher.group(1);
            if (url == null) {
                url = matcher.group(2);
            }
            if (url == null) {
                url = matcher.group(3);
            }

            if (url != null && !url.startsWith("-")) {
                request.setUrl(url);
                return;
            }
        }

        // 如果没有找到明确的URL，尝试其他模式
        if (request.getUrl() == null) {
            // 尝试匹配没有引号的URL
            Pattern fallbackPattern = Pattern.compile("\\s+(https?://[^\\s]+)");
            Matcher fallbackMatcher = fallbackPattern.matcher(command);
            if (fallbackMatcher.find()) {
                request.setUrl(fallbackMatcher.group(1));
            }
        }

        if (request.getUrl() == null) {
            throw new IllegalArgumentException("无法从cURL命令中解析出URL");
        }
    }

    /**
     * 解析请求头
     */
    private static void parseHeaders(String command, CurlRequest request) {
        Matcher matcher = HEADER_PATTERN.matcher(command);

        while (matcher.find()) {
            String header = matcher.group(1);
            if (header == null) {
                header = matcher.group(2);
            }

            if (header != null) {
                // 分割头部键值对
                int colonIndex = header.indexOf(':');
                if (colonIndex > 0) {
                    String key = header.substring(0, colonIndex).trim();
                    String value = header.substring(colonIndex + 1).trim();
                    request.getHeaders().put(key, value);
                }
            }
        }
    }

    /**
     * 解析请求体数据
     */
    private static void parseRequestBody(String command, CurlRequest request) {
        Matcher matcher = DATA_PATTERN.matcher(command);

        if (matcher.find()) {
            String data = matcher.group(1);
            if (data == null) {
                data = matcher.group(2);
            }
            if (data == null) {
                data = matcher.group(3);
            }

            if (data != null && !data.trim().isEmpty()) {
                request.setRequestBody(data);

                // 如果请求体是JSON格式，自动设置Content-Type为application/json（如果未设置）
                if (data.trim().startsWith("{") && data.trim().endsWith("}")) {
                    if (!request.getHeaders().containsKey("Content-Type") &&
                            !request.getHeaders().containsKey("content-type")) {
                        request.getHeaders().put("Content-Type", "application/json");
                    }
                }
            }
        }
    }

    /**
     * 从URL中提取查询参数
     */
    private static void extractQueryParams(CurlRequest request) {
        if (request.getUrl() != null) {
            try {
                URI uri = new URI(request.getUrl());
                String query = uri.getQuery();

                if (query != null && !query.isEmpty()) {
                    String[] pairs = query.split("&");
                    for (String pair : pairs) {
                        int idx = pair.indexOf("=");
                        String key = idx > 0 ? pair.substring(0, idx) : pair;
                        String value = idx > 0 && pair.length() > idx + 1 ?
                                pair.substring(idx + 1) : "";

                        // URL解码
                        try {
                            key = java.net.URLDecoder.decode(key, "UTF-8");
                            value = java.net.URLDecoder.decode(value, "UTF-8");
                        } catch (Exception e) {
                            // 忽略解码错误
                        }

                        request.getQueryParams().put(key, value);
                    }
                }
            } catch (URISyntaxException e) {
                // 如果URI解析失败，忽略查询参数提取
            }
        }
    }

    public static String getType(String value){
        try {
            Long.parseLong(value);
            return "number";
        }catch (Exception ignored){
        }
        if(value.equals("true") || value.equals("false") || value.equals("True") || value.equals("False")){
            return "boolean";
        }
        return "string";
    }
}