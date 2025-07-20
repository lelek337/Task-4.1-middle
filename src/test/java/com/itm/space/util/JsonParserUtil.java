package com.itm.space.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonParserUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final Logger logger = Logger.getLogger(JsonParserUtil.class.getName());

    public <T> T readObject(String body, Class<T> clazz) throws JsonParseException {
        try {
            return objectMapper.readValue(body, clazz);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, MessageFormat.format("Error parsing JSON string {0} in class {1}", body, clazz.getName()));
            throw new RuntimeException("JsonParsingFailure: " + e.getMessage());
        }
    }

    public <T> T getObjectFromJson(String filePath, Class<T> clazz) throws JsonParseException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath)) {
            return objectMapper.readValue(inputStream, clazz);
        } catch (IOException e) {
            logger.log(Level.SEVERE, MessageFormat.format("Error parsing JSON from file {0} in class {1}", filePath, clazz.getName()));
            throw new RuntimeException("JsonParsingFailure: " + e.getMessage());
        }
    }

    public String getStringFromJson(String filePath) throws JsonParseException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error converting JSON to String from file {0}", filePath);
            throw new RuntimeException("JsonParsingFailure: " + e.getMessage());
        }
    }

    public <T> List<T> getListFromJson(String filePath, Class<T> clazz) throws JsonParseException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath)) {
            return objectMapper.readValue(inputStream, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            logger.log(Level.SEVERE, MessageFormat.format("Error parsing JSON from file {0} in List<{1}>", filePath, clazz.getName()));
            throw new RuntimeException("JsonParsingFailure: " + e.getMessage());
        }
    }
}
