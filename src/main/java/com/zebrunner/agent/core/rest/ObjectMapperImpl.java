package com.zebrunner.agent.core.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import kong.unirest.GenericType;
import kong.unirest.ObjectMapper;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

final class ObjectMapperImpl implements ObjectMapper {

    private final Gson gson;

    public ObjectMapperImpl() {
        gson = new GsonBuilder()
                .registerTypeAdapter(OffsetDateTime.class, (JsonSerializer<OffsetDateTime>) (src, typeOfSrc, context) -> {
                    String formattedDate = src.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    return context.serialize(formattedDate);
                })
                .registerTypeAdapter(OffsetDateTime.class, (JsonDeserializer<OffsetDateTime>) (json, typeOfT, context) -> OffsetDateTime.parse(json.getAsString()))
                .create();
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
