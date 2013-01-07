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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.Converter;

public class ConstructorConverterBuilder {

    private final Class<?> type;
    private String[] names;
    private Converter marshaller;
    private Constructor<?> declaredConstructor;

    public ConstructorConverterBuilder(Class<?> type) {
        List<Constructor<?>> constructors = Arrays.asList(type.getDeclaredConstructors());
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(UseThis.class)) {
                UseThis annotation = constructor.getAnnotation(UseThis.class);
                names = annotation.value();
                declaredConstructor = constructor;
            }
        }
        this.type = type;
    }

    public ConstructorConverterBuilder withConstructor(Class<?> ... constructorTypes) {
        try {
            this.declaredConstructor = type.getDeclaredConstructor(constructorTypes);
        } catch (NoSuchMethodException e) {
            throw new XStreamException("Could not found declared constructor with parameters: " + Arrays.asList(constructorTypes), e);
        } catch (SecurityException e) {
            throw new XStreamException("Could not build constructor based converter", e);
        }
        return this;
    }

    public ConstructorConverterBuilder withAliases(String ... names) {
        this.names = names;
        return this;
    }
    
    public ConstructorConverterBuilder withParanamer() {
        List<Constructor<?>> constructors = Arrays.asList(type.getDeclaredConstructors());
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(UseThis.class)) {
                try {
                    Class<?> paranamerClass = Class.forName("com.thoughtworks.xstream.converters.reflection.ParanamerParser");
                    Method paramsFor = paranamerClass.getMethod("paramsFor", Constructor.class);
					names = (String[]) paramsFor.invoke(paranamerClass.newInstance(), constructor);
					declaredConstructor = constructor;
                    return this;
                } catch (IllegalArgumentException e) {
                	throw new RuntimeException("could not use paranamer", e);
                } catch (InvocationTargetException e) {
                	throw new RuntimeException("could not use paranamer",e);
                } catch (InstantiationException e) {
                	throw new RuntimeException("could not use paranamer", e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("could not use paranamer",e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("could not use paranamer",e);
                } catch (NoSuchMethodException e) {
                	throw new RuntimeException("could not use paranamer",e);
				} catch (SecurityException e) {
					throw new RuntimeException("could not use paranamer",e);
				}
            }
        }
        throw new UnsupportedOperationException("could not find annotated constructor");
    }
    
    public ConstructorConverterBuilder withMarshaller(Converter marshaller) {
        this.marshaller = marshaller;
        return this;
    }
    
    public ConstructorConverter build() {
        if (declaredConstructor == null) {
            throw new IllegalArgumentException("Could not find specified constructor");
        }
        if (names.length != declaredConstructor.getParameterTypes().length) {
            throw new IllegalArgumentException("The count of constructor parameters should be equal to xml field names conut");
        }
        return new ConstructorConverter(type, declaredConstructor, names, this.marshaller);
    }

}