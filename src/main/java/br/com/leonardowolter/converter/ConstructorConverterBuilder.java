package br.com.leonardowolter.converter;

import java.util.Arrays;

import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.Converter;

public class ConstructorConverterBuilder {

    private final Class<?> type;
    private Class<?>[] constructorTypes;

    public ConstructorConverterBuilder(Class<?> type) {
        this.type = type;
    }

    public ConstructorConverterBuilder withConstructor(Class<?> ... constructorTypes) {
        this.constructorTypes = constructorTypes;
        return this;
    }

    public Converter withAliases(String ... names) {
        if (names.length != constructorTypes.length) {
            throw new IllegalArgumentException("The count of constructor parameters should be equal to xml field names informed");
        }
        try {
            return new ConstructorConverter(type, type.getDeclaredConstructor(constructorTypes), names);
        } catch (NoSuchMethodException e) {
            throw new XStreamException("Could not found declared constructor with parameters: " + Arrays.asList(constructorTypes), e);
        } catch (SecurityException e) {
            throw new XStreamException(e);
        }
    }

}
