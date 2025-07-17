package gr.netmechanics.jmix.evrete.util;

import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for JSON serialization and deserialization using Jackson.
 *
 * @author Panos Bariamis (pbaris)
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtil {
    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.registerModule(new JavaTimeModule());
    }

    /**
     * Serializes an object to JSON string.
     *
     * @param obj the object to serialize
     * @param <T> the type of the object
     * @return an Optional containing the JSON string or empty if serialization fails
     */
    public static <T> Optional<String> toJson(final T obj) {
        try {
            return Optional.ofNullable(MAPPER.writeValueAsString(obj));

        } catch (Exception e) {
            log.warn("Failed to serialize object to JSON: {}", obj, e);
            return Optional.empty();
        }
    }

    /**
     * Deserializes a JSON string to an object of the specified class.
     *
     * @param json     the JSON string to deserialize
     * @param objClass the target class
     * @param <T>      the type of the target class
     * @return an Optional containing the deserialized object or empty if deserialization fails
     */
    public static <T> Optional<T> fromJson(final String json, final Class<T> objClass) {
        if (StringUtils.isBlank(json)) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(MAPPER.readValue(json, objClass));

        } catch (Exception e) {
            log.warn("Failed to deserialize JSON to {}: {}", objClass.getName(), json, e);
            return Optional.empty();
        }
    }

    /**
     * Deserializes a JSON string to an object of the specified type reference (for complex types).
     *
     * @param json         the JSON string to deserialize
     * @param typeReference the target type reference
     * @param <T>          the type of the target object
     * @return an Optional containing the deserialized object or empty if deserialization fails
     */
    public static <T> Optional<T> fromJson(final String json, final TypeReference<T> typeReference) {
        if (StringUtils.isBlank(json)) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(MAPPER.readValue(json, typeReference));

        } catch (Exception e) {
            log.warn("Failed to deserialize JSON to {}: {}", typeReference.getType(), json, e);
            return Optional.empty();
        }
    }
}