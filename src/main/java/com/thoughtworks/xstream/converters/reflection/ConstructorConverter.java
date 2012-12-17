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