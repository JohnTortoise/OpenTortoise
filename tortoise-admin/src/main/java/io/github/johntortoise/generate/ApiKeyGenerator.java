package io.github.johntortoise.generate;

import java.util.UUID;


public class ApiKeyGenerator {

    public static String generateUUIDKey() {
        return "sk-" + UUID.randomUUID().toString();
    }

}