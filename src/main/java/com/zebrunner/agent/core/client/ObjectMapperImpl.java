package com.zebrunner.agent.core.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import kong.unirest.GenericType;
import kong.unirest.ObjectMapper;

import java.time.Instant;
import java.time.OffsetDateTime;

final class ObjectMapperImpl implements ObjectMapper {

    private final Gson gson;

    public ObjectMapperImpl() {
        gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class, instantSerializer())
                .registerTypeAdapter(Instant.class, instantDeserializer())
                .registerTypeAdapter(OffsetDateTime.class, offsetDateTimeSerializer())
                .registerTypeAdapter(OffsetDateTime.class, offsetDateTimeDeserializer())
                .create();
    }

    private JsonSerializer<Instant> instantSerializer() {
        return (src, typeOfSrc, context) -> context.serialize(src.toString());
    }

    private JsonDeserializer<Instant> instantDeserializer() {
        return (json, typeOfT, context) -> Instant.parse(json.getAsString());
    }

    private JsonSerializer<OffsetDateTime> offsetDateTimeSerializer() {
        return (src, typeOfSrc, context) -> context.serialize(src.toString());
    }

    private JsonDeserializer<OffsetDateTime> offsetDateTimeDeserializer() {
        return (json, typeOfT, context) -> OffsetDateTime.parse(json.getAsString());
    }

    @Override
    public <T> T readValue(String value, Class<T> valueType) {
        return gson.fromJson(value, valueType);
    }

    @Override
    public <T> T readValue(String value, GenericType<T> genericType) {
        return gson.fromJson(value, genericType.getType());
    }

    @Override
    public String writeValue(Object value) {
        return gson.toJson(value);
    }
}
