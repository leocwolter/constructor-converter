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


import java.util.Calendar;
import java.util.List;

import junit.framework.TestCase;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotation.UnmarshallingConstructor;
import com.thoughtworks.xstream.converters.Converter;

public class ConstructorConverterTest extends TestCase {

    @SuppressWarnings("unused")
    private List<Product> products;
    private XStream xStream;

    private static class Order {

        private final String id;
        private final List<Product> products;
        private final Calendar date;
        private final String buyer;

        protected Order(String id, List<Product> products, Calendar date, String buyer) {
            this.id = id;
            this.products = products;
            this.date = date;
            this.buyer = buyer;
        }
        
        @SuppressWarnings("unused")
        protected Order(Calendar date) {
            this(null, null, date, null);
        }
        
        @SuppressWarnings("unused")
        protected Order(List<Product> products) {
            this(null, products, null, null);
        }
        
        @SuppressWarnings("unused")
        protected Order(String id) {
            this(id, null, null, null);
        }

    }

    public static class Product {
        private final String name;
        @SuppressWarnings("unused")
        private final User user;

        protected Product(String name) {
            this.name = name;
            this.user = null;
        }
        
        protected Product(User user) {
            this.name = null;
            this.user = user;
        }    }
    
    public static class User {
        @SuppressWarnings("unused")
        private final String name;
        
        protected User(String name) {
            this.name = name;
        }
    }
    
    public static class AnnotatedUser {
        private final String name;
        
        @UnmarshallingConstructor({"first-name","last-name"})
        protected AnnotatedUser(String firstName, String lastName) {
            this.name = firstName + " "+ lastName;
        }
    }
    
    public static class ParanamerUser {
        private final String name;
        
        @UnmarshallingConstructor
        protected ParanamerUser(String strangeArgName) {
            this.name = strangeArgName;
        }
    }
    
    @Override
    public void setUp() {
        xStream = new XStream();
        xStream.alias("order", Order.class);
        xStream.alias("product", Product.class);
        xStream.alias("user", User.class);
        xStream.alias("annotateduser", AnnotatedUser.class);
        xStream.alias("paranameruser", ParanamerUser.class);
    }
    
    public void testShouldBuildOrderWithIdOnlyTest() {
        Converter converter = ConstructorConverter.forType(Order.class)
            .withConstructor(String.class)
            .withAliases("id").build();
        xStream.registerConverter(converter);
        Order order = (Order) xStream.fromXML("<order><id>666</id><name>some name</name></order>");
        assertEquals("666", order.id);
    }
    
    public void testShouldBuildCalendar() {
        Converter converter = ConstructorConverter.forType(Order.class)
            .withConstructor(Calendar.class)
            .withAliases("date").build();
        xStream.registerConverter(converter);
        Order order = (Order) xStream.fromXML("<order>" +
                "<date>" +
                "<time>1352913901530</time>" +
                "<timezone>America/Sao_Paulo</timezone>" +
                "</date>" +
                "</order>");
        assertNotNull(order.date);
        assertEquals(1352913901530l, order.date.getTimeInMillis());
    }
    
    public void testShouldBuildCalendarIgnoringFirstDate() {
        Converter converter = ConstructorConverter.forType(Order.class)
            .withConstructor(Calendar.class)
            .withAliases("date2").build();
        xStream.registerConverter(converter);
        Order order = (Order) xStream.fromXML("<order>" +
                "<date>" + 
                "<time>1352913901530</time>" +
                "<timezone>America/Sao_Paulo</timezone>"+
                "</date>" +
                "<date2>" + 
                "<time>1352913901531</time>" +
                "<timezone>America/Sao_Paulo</timezone>"+
                "</date2>" +
                "</order>");
        assertNotNull(order.date);
        assertEquals(1352913901531l, order.date.getTimeInMillis());
    }
    
    public void testShouldUseAnnotatedConstructor() {
        ConstructorConverter converter = ConstructorConverter.forType(AnnotatedUser.class).build();
        xStream.registerConverter(converter);
        String xml = "<annotateduser><first-name>user</first-name><last-name>name</last-name></annotateduser>";
        AnnotatedUser user = (AnnotatedUser) xStream.fromXML(xml);
        assertEquals("user name", user.name);
    }
    
    public void testShouldUseParanamerToDiscoverParameters() {
        ConstructorConverter converter = ConstructorConverter.forType(ParanamerUser.class).withParanamer().build();
        xStream.registerConverter(converter);
        String xml = "<paranameruser><strangeArgName>with paranamer</strangeArgName></paranameruser>";
        ParanamerUser user = (ParanamerUser) xStream.fromXML(xml);
        assertEquals("with paranamer", user.name);
    }
    
    public void testShouldBuildOrderWithAllSpecifiedParameters() {
        Converter orderConverter = ConstructorConverter.forType(Order.class)
            .withConstructor(String.class, List.class, Calendar.class, String.class)
            .withAliases("id", "products", "date", "buyer")
            .build();
            
        Converter productConverter = ConstructorConverter.forType(Product.class)
            .withConstructor(String.class)
            .withAliases("name")
            .build();
        
        xStream.registerConverter(orderConverter);
        xStream.registerConverter(productConverter);
        Order order = (Order) xStream.fromXML(
                "<order>" +
                        "<id>666</id>" +
                        "<products>" +
                            "<product>" +
                            "<name>first product</name>" +
                            "</product>" +
                            "<product>" +
                            "<name>second product</name>" +
                            "</product>" +
                        "</products>" +
                        "<date>" + 
                            "<time>1352913901530</time>" +
                            "<timezone>America/Sao_Paulo</timezone>"+
                        "</date>" +
                        "<buyer>buyer name</buyer>" +
                        "<useless>something</useless>" +
                "</order>");
        assertEquals(2, order.products.size());
        assertEquals("first product", order.products.get(0).name);
        assertEquals("second product", order.products.get(1).name);
        assertNotNull(order.date);
        assertEquals("buyer name", order.buyer);
    }

    public void testShouldNotMarshall() {
        Converter converter = ConstructorConverter.forType(User.class)
            .withConstructor(String.class)
            .withAliases("name").build();
        
        xStream.registerConverter(converter);
        User user = new User("user name");
        
        try {
            xStream.toXML(user);
            fail("should throw exception");
        } catch (UnsupportedOperationException e) {
        }
    }
    
    public void testShouldMarshallWithProvidedConverter() {
        Converter reflectionConverter = xStream.getConverterLookup().lookupConverterForType(Order.class);
        
        Converter converter = ConstructorConverter.forType(User.class)
            .withConstructor(String.class)
            .withMarshaller(reflectionConverter)
            .withAliases("name").build();
        
        xStream.registerConverter(converter);
        User user = new User("user name");
        
        String xml = xStream.toXML(user);
        String xmlExpected = "<user>\n" +
                "  <name>user name</name>\n" +
                "</user>";
        
        assertEquals(xmlExpected, xml);
    }
}