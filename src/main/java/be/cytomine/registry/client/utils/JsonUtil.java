package io.github.ya_b.registry.client.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JsonUtil {
    private static final Gson GSON = new GsonBuilder().create();

    public static <T> T fromJson(String content, TypeToken<T> typeToken) {
        return GSON.fromJson(content, typeToken);
    }

    public static <T> T fromJson(String content, Class<T> clazz) {
        return GSON.fromJson(content, clazz);
    }

    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }
}
