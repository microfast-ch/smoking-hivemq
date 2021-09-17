package ch.microfast.hivemq.smoker.authz.serialization;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

/**
 * JSON serializer based on jackson {@link ObjectMapper} class.
 * Configuration:
 * <ul>
 *     <li>Lower camel case</li>
 *     <li>Case insensitive enum values</li>
 *     <li>Case insensitive property values</li>
 *     <li>Sort properties alphabetically (for consistent claim signature verification)</li>
 * </ul>
 */
public class SmokerJsonSerializer extends ObjectMapper {
    public SmokerJsonSerializer() {
        super();
        this.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        this.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        this.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        this.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
    }
}
