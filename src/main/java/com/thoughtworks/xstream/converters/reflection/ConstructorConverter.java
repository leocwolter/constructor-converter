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
package com.thoughtworks.xstream.converters.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    private ConstructorConverter(Class<?> type, Constructor<?> declaredConstructor, String[] fieldNames, Converter marshaller) {
        this.type = type;
        this.marshaller = marshaller;
        declaredConstructor.setAccessible(true);
        this.declaredConstructor = declaredConstructor;
        this.names = Arrays.asList(fieldNames);
    }

    public static ConstructorConverterBuilder forType(Class<?> type){
    	return new ConstructorConverterBuilder(type);
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

    /**
     * A builder for ConstructoConverter
     *
     * @author Francisco Sokol
     * @author Leonardo Wolter
     * @author Guilherme Silveira 
     */
    public static class ConstructorConverterBuilder {

        private final Class<?> type;
        private String[] names;
        private Converter marshaller;
        private Constructor<?> declaredConstructor;

        /**
         * Constructor 
         * @param type The class the converter will convert  
         */
        private ConstructorConverterBuilder(Class<?> type) {
            List<Constructor<?>> constructors = Arrays.asList(type.getDeclaredConstructors());
            for (Constructor<?> constructor : constructors) {
                if (constructor.isAnnotationPresent(UnmarshallingConstructor.class)) {
                    UnmarshallingConstructor annotation = constructor.getAnnotation(UnmarshallingConstructor.class);
                    names = annotation.value();
                    declaredConstructor = constructor;
                }
            }
            this.type = type;
        }

        /**
         * Specify the types of the constructor which the converter will use 
         *
         * @param constructorTypes the classes of the chosen constructor
         * @return the builder
         */
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

        /**
         * Specify the names of the xml nodes to be mapped to the constructor
         *
         * @param names the names of the xml nodes. 
         * @return the builder
         */
        public ConstructorConverterBuilder withAliases(String ... names) {
            this.names = names;
            return this;
        }
        
        /**
         * Enable the use of paranamer to discover the constructor parameters names 
         *
         * @return the builder
         */
        public ConstructorConverterBuilder withParanamer() {
            List<Constructor<?>> constructors = Arrays.asList(type.getDeclaredConstructors());
            for (Constructor<?> constructor : constructors) {
                if (constructor.isAnnotationPresent(UnmarshallingConstructor.class)) {
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
        
        /**
         * Defines the marshaller that the converter will use. 
         *
         * @return the builder
         */
        public ConstructorConverterBuilder withMarshaller(Converter marshaller) {
            this.marshaller = marshaller;
            return this;
        }
        
        /**
         * Instatiates the converter based on the builder state 
         *
         * @return the converter built 
         */
        public ConstructorConverter build() {
            if (declaredConstructor == null) {
                throw new IllegalArgumentException("Could not find specified constructor");
            }
            if (names.length != declaredConstructor.getParameterTypes().length) {
                throw new IllegalArgumentException("The count of constructor parameters should be equal to xml field names conut");
            }
            return new ConstructorConverter(type, declaredConstructor, names, marshaller);
        }

    }

}