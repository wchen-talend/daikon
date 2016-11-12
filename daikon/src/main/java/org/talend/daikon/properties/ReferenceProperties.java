// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.daikon.properties;

import static org.talend.daikon.properties.property.PropertyFactory.*;

import java.lang.reflect.Field;

import org.talend.daikon.properties.property.Property;

/**
 * A reference to another properties.
 * 
 * The {@link org.talend.daikon.properties.presentation.WidgetType#COMPONENT_REFERENCE} uses this class as its
 * properties and the Widget will populate these values.
 */

public class ReferenceProperties<T extends Properties> extends PropertiesImpl {

    /**
     * name of the definition that may be used to create the reference type.
     * the Generic type T for this class must be created by the defition matching the referenceDefintionName.
     */
    public final Property<String> referenceDefintionName = newProperty("referenceDefintionName");

    /**
     * the reference instance
     */
    private transient T reference;

    public ReferenceProperties(String name, String referenceDefintionName) {
        super(name);
        this.referenceDefintionName.setValue(referenceDefintionName);
    }

    public void setReference(Properties prop) {
        reference = (T) prop;
    }

    public T getReference() {
        return reference;
    }

    @Override
    protected boolean acceptUninitializedField(Field f) {
        if (super.acceptUninitializedField(f)) {
            return true;
        }
        // we accept that return field is not intialized after setupProperties.
        return "reference".equals(f.getName());
    }

}
