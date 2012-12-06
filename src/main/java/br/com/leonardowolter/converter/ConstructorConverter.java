package br.com.leonardowolter.converter;

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

    public ConstructorConverter(Class<?> type, Constructor<?> declaredConstructor, String[] fieldNames) {
        this.type = type;
        declaredConstructor.setAccessible(true);
        this.declaredConstructor = declaredConstructor;
        this.names = Arrays.asList(fieldNames);
    }

    @Override
    @SuppressWarnings("rawtypes") 
    public boolean canConvert(Class type) {
        return this.type.equals(type);
    }

    @Override
    public void marshal(Object arg0, HierarchicalStreamWriter arg1, MarshallingContext arg2) {
        throw new UnsupportedOperationException("");
    }

    @Override
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
