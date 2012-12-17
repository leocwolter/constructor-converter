package com.thoughtworks.xstream.converters.reflection;

import java.lang.reflect.Constructor;
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
                    ParanamerParser parser = (ParanamerParser) Class.forName("com.thoughtworks.xstream.converters.reflection.ParanamerParser").newInstance();
                    names = parser.paramsFor(constructor);
                    declaredConstructor = constructor;
                    return this;
                } catch (InstantiationException e) {
                    throw new RuntimeException("could not use paranamer", e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("could not use paranamer",e);
                } catch (ClassNotFoundException e) {
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