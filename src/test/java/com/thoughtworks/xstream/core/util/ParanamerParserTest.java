/*
 * Copyright (C) 2013 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 07. January 2013 by Leonardo Wolter & Francisco Sokol & Guilherme Silveira
 */
package com.thoughtworks.xstream.core.util;

import static junit.framework.Assert.assertEquals;

import java.lang.reflect.Constructor;

import org.junit.Test;

public class ParanamerParserTest{
    
    @SuppressWarnings("unused")
    private static class SampleClass {
        public SampleClass(String arg1, String arg2) {
        }
    }

    @Test
    public void testShouldFindParameters() throws Exception {
        Constructor<?> c = SampleClass.class.getDeclaredConstructors()[0];
        String[] paramsFor = new ParanamerParser().paramsFor(c);
        assertEquals(2, paramsFor.length);
    }
}
