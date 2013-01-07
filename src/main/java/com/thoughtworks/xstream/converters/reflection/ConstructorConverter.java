/*
 * Copyright (C) 2006, 2007, 2008, 2009 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 07. January 2013 by Leonardo Wolter & Francisco Sokol & Guilherme Silveira
 */
package com.thoughtworks.xstream.converters.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * <p>ConstructorConverter which can use 3 different strategies to unmarshall fields
 * <ul>
 * <li> Declaring the constructor to be used and the xml' nodes aliases</li>
 * <li> Annotating the constructor and declaring the xml' nodes aliases in that annotation</li>
 * <li> Annotating the constructor and using Paranamer to imply the xml' nodes aliases</li>
 * </ul>
 * </p>
 * <p>ConstructorConverter does not support the marshall opperation</p>
 * @author Leonardo Wolter
 * @author Francisco Sokol
 * @author Guilherme Silveira
 */
public class ConstructorConverter implements Converter {

    private final Class<?> type;
    private final Constructor<?> declaredConstructor;
    private final List<String> names;
    private final Converter marshaller;

    public ConstructorConverter(Class<?> type, Constructor<?> declaredConstructor, String[] fieldNames, Converter marshaller) {
        this.type = type;
        this.marshaller = marshaller;
        declaredConstructor.setAccessible(true);
        this.declaredConstructor = declaredConstructor;
        this.names = Arrays.asList(fieldNames);
    }

    @SuppressWarnings("rawtypes") 
    public boolean canConvert(Class type) {
        return this.type.equals(type);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        if (marshaller == null)
            throw new UnsupportedOperationException("");
        else
            marshaller.marshal(source, writer, context);
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Object[] constructorParameters = new Object[names.size()];
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            if (names.contains(nodeName)) {
                int position = names.indexOf(nodeName);
                constructorParameters[position] = context.convertAnother(null, declaredConstructor.getParameterTypes()[position]);
            }
            reader.moveUp();
        }
        try {
            return declaredConstructor.newInstance(constructorParameters);
        } catch (InstantiationException e) {
            throw new XStreamException(e);
        } catch (IllegalAccessException e) {
            throw new XStreamException(e);
        } catch (IllegalArgumentException e) {
            throw new XStreamException(e);
        } catch (InvocationTargetException e) {
            throw new XStreamException(e);
        }
    }


}