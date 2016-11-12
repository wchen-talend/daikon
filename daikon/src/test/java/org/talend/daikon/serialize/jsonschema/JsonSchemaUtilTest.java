package org.talend.daikon.serialize.jsonschema;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.serialize.jsonschema.ReferenceExampleProperties.TestAProperties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSchemaUtilTest {

    @Test
    public void test() throws URISyntaxException, IOException, ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException {
        String jsonDataStr = readJson("FullExampleJsonData.json");

        FullExampleProperties properties = JsonSchemaUtil.fromJson(jsonDataStr,
                (FullExampleProperties) new FullExampleProperties("fullexample").init());

        String jsonStr = readJson("FullExampleProperties.json");
        String jsonResult = JsonSchemaUtil.toJson(properties);
        assertEquals(jsonStr, jsonResult);
    }

    @Test
    public void testSerializeUnserialize() throws ParseException, JsonProcessingException, IOException {
        // create a json string of a setup properties
        FullExampleProperties fep = JsonDataGeneratorTest.createASetupFullExampleProperties();
        String json = JsonSchemaUtil.toJson(fep);
        // re-create the Properties from the json data of the json string
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json);
        JsonNode jsonData = jsonNode.get(JsonSchemaUtil.TAG_JSON_DATA);
        FullExampleProperties deserFep = JsonSchemaUtil.fromJson(jsonData.toString(),
                (FullExampleProperties) new FullExampleProperties("fullexample").init());
        // compare them
        assertEquals(fep, deserFep);
    }

    @Test
    public void testSerializeResolveReferenceProperties() throws ParseException, JsonProcessingException, IOException {
        // create a json string of a setup properties
        ReferenceExampleProperties referenceProperties = ReferenceExampleProperties.createASetupReferenceExampleProperties();
        referenceProperties.parentProp.setValue("foo");
        referenceProperties.testAPropReference.getReference().aProp.setValue("bar");
        String parentJson = JsonSchemaUtil.toJson(referenceProperties);
        System.out.println(parentJson);
        String childJson = JsonSchemaUtil.toJson(referenceProperties.testAPropReference.getReference());

        // re-create the Properties from the json data of the json string
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(parentJson);
        JsonNode jsonData = jsonNode.get(JsonSchemaUtil.TAG_JSON_DATA);
        ReferenceExampleProperties deserRefExampleProp = JsonSchemaUtil.fromJson(jsonData.toString(),
                (ReferenceExampleProperties) new ReferenceExampleProperties("refexample").init());

        jsonNode = mapper.readTree(childJson);
        jsonData = jsonNode.get(JsonSchemaUtil.TAG_JSON_DATA);
        TestAProperties deserTestAProp = JsonSchemaUtil.fromJson(jsonData.toString(),
                (TestAProperties) new TestAProperties("reference").init());

        // merge everything to the parent
        Map<String, Properties> definition2PropertiesMap = new HashMap<>();
        definition2PropertiesMap.put("no_used", deserRefExampleProp);
        definition2PropertiesMap.put(TestAProperties.TEST_A_PROPERTIES_DEFINTION_NAME, deserTestAProp);
        JsonSchemaUtil.resolveReferenceProperties(definition2PropertiesMap);

        // compare the parent with the initial object
        assertEquals(referenceProperties, deserRefExampleProp);
        assertEquals(deserRefExampleProp.testAPropReference.getReference(), deserTestAProp);
    }

    public static String readJson(String path) throws URISyntaxException, IOException {
        java.net.URL url = JsonSchemaUtilTest.class.getResource(path);
        java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
        return new String(java.nio.file.Files.readAllBytes(resPath), "UTF8").trim();
    }

}
