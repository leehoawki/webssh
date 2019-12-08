package com.kedacom.ctsp.webssh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class WebSSHUtil {
    private static final Logger LOG = LoggerFactory.getLogger(WebSSHUtil.class);

    public static <T> String objectToJson(T obj) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static JsonNode strToJsonObject(String data) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readTree(data);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static ObjectNode createObjectNode() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.createObjectNode();
    }

    public static ArrayNode createArrayNode() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.createArrayNode();
    }
}
