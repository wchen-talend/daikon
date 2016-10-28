package org.talend.daikon.services;

import org.talend.daikon.annotation.ServiceImplementation;

@ServiceImplementation
public class TestServiceImpl implements TestService {

    @Override
    public String sayHi() {
        return I_SAY_HI;
    }

    @Override
    public String sayHiWithMyName(String name) {
        return "Hi " + name;
    }
}
