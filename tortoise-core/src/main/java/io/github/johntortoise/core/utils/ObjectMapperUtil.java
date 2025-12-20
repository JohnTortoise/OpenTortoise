package io.github.johntortoise.core.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperUtil {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private ObjectMapperUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static ObjectMapper getInstance() {
        return OBJECT_MAPPER;
    }

    public static ObjectMapper createObjectMapper() {
        return OBJECT_MAPPER;
    }
}
