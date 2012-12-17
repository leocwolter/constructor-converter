package com.thoughtworks.xstream.converters.reflection;

import java.lang.reflect.Constructor;

import junit.framework.TestCase;

public class ParanamerParserTest extends TestCase {
    
    @SuppressWarnings("unused")
    private static class SampleClass {
        public SampleClass(String arg1, String arg2) {
        }
    }

    public void testShouldFindParameters() throws Exception {
        Constructor<?> c = SampleClass.class.getDeclaredConstructors()[0];
        System.out.println(c);
        String[] paramsFor = new ParanamerParser().paramsFor(c);
        assertEquals(2, paramsFor.length);
    }
}
