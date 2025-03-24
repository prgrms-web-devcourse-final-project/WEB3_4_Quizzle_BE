package com.ll.quizzle.standard.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.quizzle.global.app.AppConfig;
import lombok.SneakyThrows;

import java.util.Map;

public class Ut {

    public static class str {
        public static boolean isBlank(String str) {
            return str == null || str.trim().isEmpty();
        }
    }

    public static class json {
        private static final ObjectMapper om = AppConfig.getObjectMapper();

        @SneakyThrows
        public static String toString(Object obj) {
            return om.writeValueAsString(obj);
        }

        @SneakyThrows
        public static Map<String, Object> toMap(String jsonStr) {
            return om.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
        }
    }

}
