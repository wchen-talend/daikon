package org.talend.daikon.serialize.jsonschema;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.PropertiesVisitor;
import org.talend.daikon.properties.ReferenceProperties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Util for round trip between ComponentProperties and JSONSchema/UISchema/JSONData
 */
public class JsonSchemaUtil {

    static final Logger LOG = LoggerFactory.getLogger(JsonSchemaUtil.class);

    public static final String TAG_JSON_SCHEMA = "jsonSchema";

    public static final String TAG_JSON_UI = "uiSchema";

    public static final String TAG_JSON_DATA = "properties";

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator();

    private static final UiSchemaGenerator uiSchemaGenerator = new UiSchemaGenerator();

    private static final JsonDataGenerator jsonDataGenerator = new JsonDataGenerator();

    private static final JsonPropertiesResolver resolver = new JsonPropertiesResolver();

    /**
     * fills the initalInstance with the properties from the Json-data String
     */
    public static <P extends Properties> P fromJson(String jsonStr, P initialInstance) {
        JsonNode jsonNode;
        try {
            jsonNode = mapper.readTree(jsonStr);
            return fromJson(jsonNode, initialInstance);
        } catch (IOException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException
                | ClassNotFoundException e) {
            throw TalendRuntimeException.createUnexpectedException(e);
        }
    }

    /**
     * fills the initalInstance with the properties from the Json-data intput stream
     */
    public static <P extends Properties> P fromJson(InputStream jsonIS, P initialInstance) {
        try {
            JsonNode jsonNode = mapper.readTree(jsonIS);
            return fromJson(jsonNode, initialInstance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <P extends Properties> P fromJson(JsonNode jsonData, P initialInstance) throws NoSuchMethodException,
            IOException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        if (jsonData == null) {
            throw TalendRuntimeException
                    .createUnexpectedException(TAG_JSON_SCHEMA + " or " + TAG_JSON_DATA + " should not be null");
        }
        return resolver.resolveJson((ObjectNode) jsonData, initialInstance);
    }

    public static String toJson(Properties cp) {
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.set(TAG_JSON_SCHEMA, jsonSchemaGenerator.genSchema(cp));
        objectNode.set(TAG_JSON_DATA, jsonDataGenerator.genData(cp));
        if (!cp.getForms().isEmpty()) {
            objectNode.set(TAG_JSON_UI, uiSchemaGenerator.genWidget(cp));
        }
        return objectNode.toString();
    }

    /**
     * resolve the referenced properties between a group of properties.
     * 
     * @param propertiesMap a map with the definitions name and Properties instance related to those definitions
     */
    public static void resolveReferenceProperties(final Map<String, Properties> propertiesMap) {
        for (Entry<String, Properties> entry : propertiesMap.entrySet()) {
            Properties theProperties = entry.getValue();
            theProperties.accept(new PropertiesVisitor() {

                @Override
                public void visit(Properties properties, Properties parent) {
                    if (properties instanceof ReferenceProperties<?>) {
                        ReferenceProperties<?> referenceProperties = (ReferenceProperties<?>) properties;
                        Properties reference = propertiesMap.get(referenceProperties.referenceDefintionName.getValue());
                        if (reference != null) {
                            referenceProperties.setReference(reference);
                        } else {// no reference of the required type has been provided so do no set anything but log it
                            LOG.debug("failed to find a reference object for ReferenceProperties[" + referenceProperties.getName()
                                    + "] with defintion [" + referenceProperties.referenceDefintionName.getValue()
                                    + "] and with parent type [" + (parent != null ? parent.getClass().getName() : "null") + "]");
                        }
                    }

                }
            }, null);
        }
    }
}
