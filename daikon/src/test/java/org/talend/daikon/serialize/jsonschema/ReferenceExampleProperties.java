package org.talend.daikon.serialize.jsonschema;

import static org.talend.daikon.properties.property.PropertyFactory.*;

import java.text.ParseException;

import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.property.Property;

public class ReferenceExampleProperties extends PropertiesImpl {

    public Property<String> parentProp = newString("parentProp", "initialparentValue");

    public ReferenceProperties<TestAProperties> testAPropReference = new ReferenceProperties<>("testAPropReference",
            TestAProperties.TEST_A_PROPERTIES_DEFINTION_NAME);

    public ReferenceExampleProperties(String name) {
        super(name);
    }

    public static class TestAProperties extends PropertiesImpl {

        public static final String TEST_A_PROPERTIES_DEFINTION_NAME = "TestAPropertiesDefintionName";

        public Property<String> aProp = newString("aProp", "initialaPropValue");

        public TestAProperties(String name) {
            super(name);
        }

    }

    static public ReferenceExampleProperties createASetupReferenceExampleProperties() throws ParseException {
        ReferenceExampleProperties properties = (ReferenceExampleProperties) new ReferenceExampleProperties("refexample").init();
        TestAProperties theReferencedProperties = (TestAProperties) new TestAProperties(null).init();
        properties.testAPropReference.setReference(theReferencedProperties);
        return properties;
    }

}
