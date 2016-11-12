// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.daikon.serialize.jsonschema;

import static org.junit.Assert.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.serialize.jsonschema.ReferenceExampleProperties.TestAProperties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ReferencePropertiesSerializationTest {

    @Test
    public void testSerializeUnserializeWithReferenceProperties() throws ParseException, JsonProcessingException, IOException {
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

}
