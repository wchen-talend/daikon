package org.talend.daikon.serialize.jsonschema;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.definition.service.DefinitionRegistryService;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.serialize.jsonschema.ReferenceExampleProperties.TestAProperties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import shaded.org.apache.commons.io.IOUtils;

public class JsonSchemaUtilTest {

    @Test
    public void testUnserializeWithInstance() throws ParseException, JsonProcessingException, IOException {
        // create a json string of a setup properties
        FullExampleProperties fep = JsonDataGeneratorTest.createASetupFullExampleProperties();
        ObjectNode propertiesData = new JsonDataGenerator().processTPropertiesData(fep);
        FullExampleProperties deserFep = JsonSchemaUtil.fromJson(propertiesData.toString(),
                (FullExampleProperties) new FullExampleProperties("fullexample").init());
        // compare them
        assertEquals(fep, deserFep);
    }

    @Test
    public void testfromJsonNode() throws ParseException, JsonProcessingException, IOException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        DefinitionRegistryService defRegServ = getRegistryWithDef1();

        // create a json string of a setup properties
        FullExampleProperties fep = JsonDataGeneratorTest.createASetupFullExampleProperties();
        String json = new JsonDataGenerator().processTPropertiesData(fep).toString();

        // check instance is deserialized properly
        // re-create the Properties from the json data of the json string
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonData = (ObjectNode) mapper.readTree(json);
        jsonData.put(JsonSchemaConstants.DEFINITION_NAME_JSON_METADATA, "def1");
        FullExampleProperties deserFep = (FullExampleProperties) JsonSchemaUtil.fromJsonNode(defRegServ, jsonData);
        // compare them
        assertEquals(fep, deserFep);

        // check that if no definition node found we have the right exception
        mapper = new ObjectMapper();
        jsonData = (ObjectNode) mapper.readTree(json);
        try {
            deserFep = (FullExampleProperties) JsonSchemaUtil.fromJsonNode(defRegServ, jsonData);
            fail("should have thrown an exception");
        } catch (TalendRuntimeException e) {
            assertTrue(e.getCode() == CommonErrorCodes.UNABLE_TO_PARSE_JSON);
        }

        // check that if wrong definition found we have the right exception
        mapper = new ObjectMapper();
        jsonData = (ObjectNode) mapper.readTree(json);
        jsonData.put(JsonSchemaConstants.DEFINITION_NAME_JSON_METADATA, "unregistered_def");
        try {
            deserFep = (FullExampleProperties) JsonSchemaUtil.fromJsonNode(defRegServ, jsonData);
            fail("should have thrown an exception");
        } catch (TalendRuntimeException e) {
            assertTrue(e.getCode() == CommonErrorCodes.UNREGISTERED_DEFINITION);
            assertTrue(e.getContext().get("definitionName") == "unregistered_def");
        }

    }

    @Test
    public void testUnserializeWithDefinitionName()
            throws ParseException, JsonProcessingException, IOException, URISyntaxException {
        FullExampleProperties fep = JsonDataGeneratorTest.createASetupFullExampleProperties();
        String json = new JsonDataGenerator().genData(fep, "def1").toString();
        FullExampleProperties deserFep = (FullExampleProperties) JsonSchemaUtil.fromJson(json, getRegistryWithDef1());
        // compare them
        assertEquals(fep, deserFep);
    }

    @Test
    public void testUnserializeWithDefinitionNameAndStream() throws ParseException, JsonProcessingException, IOException {
        FullExampleProperties fep = JsonDataGeneratorTest.createASetupFullExampleProperties();
        ObjectNode jsonNode = new JsonDataGenerator().genData(fep, "def1");
        try (InputStream is = IOUtils.toInputStream(jsonNode.toString())) {
            FullExampleProperties deserFep = (FullExampleProperties) JsonSchemaUtil.fromJson(is, getRegistryWithDef1());
            // compare them
            assertEquals(fep, deserFep);
        }
    }

    public DefinitionRegistryService getRegistryWithDef1() {
        DefinitionRegistryService defRegServ = mock(DefinitionRegistryService.class);
        Definition exampleDef = mock(Definition.class);
        when(exampleDef.createProperties()).thenReturn(new FullExampleProperties(""));
        when(defRegServ.getDefinitionsMapByType(Definition.class)).thenReturn(Collections.singletonMap("def1", exampleDef));
        return defRegServ;
    }

    @Test
    public void testSerializeWithDefinitionName() throws URISyntaxException, IOException, ClassNotFoundException,
            NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        String propertiesJsonStr = JsonSchemaUtil.toJson(new FullExampleProperties("fullexample").init(), "ZeDefinitionName");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(propertiesJsonStr);
        ObjectNode jsonData = (ObjectNode) jsonNode.get(JsonSchemaUtil.TAG_JSON_DATA);
        JsonNode defNameNode = jsonData.get(JsonSchemaConstants.DEFINITION_NAME_JSON_METADATA);
        assertNotNull(defNameNode);
        assertEquals(defNameNode.asText(), "ZeDefinitionName");
    }

    @Test
    public void testSerializeResolveReferenceProperties() throws ParseException, JsonProcessingException, IOException {
        DefinitionRegistryService registryWithDef1 = getRegistryWithDef1();
        // create a json string of a setup properties
        ReferenceExampleProperties referenceProperties = ReferenceExampleProperties.createASetupReferenceExampleProperties();
        referenceProperties.parentProp.setValue("foo");
        referenceProperties.testAPropReference.getReference().aProp.setValue("bar");
        String parentJson = JsonSchemaUtil.toJson(referenceProperties, "def1");
        System.out.println(parentJson);
        String childJson = JsonSchemaUtil.toJson(referenceProperties.testAPropReference.getReference(), "def1");

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
