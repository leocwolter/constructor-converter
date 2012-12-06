package br.com.leonardowolter.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Calendar;
import java.util.List;

import org.junit.Test;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;

public class ConstructorConverterTest {

    private List<Product> products;

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
        
        protected Order(Calendar date) {
            this(null, null, date, null);
        }
        
        protected Order(List<Product> products) {
        	this(null, products, null, null);
        }
        
        protected Order(String id) {
            this(id, null, null, null);
        }

    }

    public static class Product {
        private final String name;
		private final User user;

		protected Product(String name) {
			this.name = name;
			this.user = null;
        }
        
		protected Product(User user) {
        	this.name = null;
			this.user = user;
        }
    }
    
    public static class User {
    	private final String name;
    	
    	protected User(String name) {
    		this.name = name;
    	}
    }
    
    @Test
    public void shouldBuildOrderWithAllParameters() {
        Converter orderConverter = new ConstructorConverterBuilder(Order.class)
            .withConstructor(String.class, List.class, Calendar.class, String.class)
            .withAliases("id", "products", "date", "buyer");
        
        Converter productConverter = new ConstructorConverterBuilder(Product.class)
            .withConstructor(String.class)
            .withAliases("name");
      
        XStream xStream = new XStream();
        xStream.registerConverter(orderConverter);
        xStream.registerConverter(productConverter);
        xStream.alias("order", Order.class);
        xStream.alias("product", Product.class);
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
        		"</order>");
        assertEquals(2, order.products.size());
        assertEquals("first product", order.products.get(0).name);
        assertEquals("second product", order.products.get(1).name);
        assertNotNull(order.date);
        assertEquals("buyer name", order.buyer);
    }
    
    @Test
    public void shouldBuildOrderWithIdOnly() {
        Converter converter = new ConstructorConverterBuilder(Order.class).withConstructor(String.class).withAliases("id");
        XStream xStream = new XStream();
        xStream.alias("order", Order.class);
        xStream.registerConverter(converter);
        Order order = (Order) xStream.fromXML("<order><id>666</id><name>some name</name></order>");
        assertEquals("666", order.id);
    }
    
    @Test
    public void shouldBuildCalendar() {
        Converter converter = new ConstructorConverterBuilder(Order.class).withConstructor(Calendar.class).withAliases("date");
        XStream xStream = new XStream();
        xStream.alias("order", Order.class);
        xStream.registerConverter(converter);
        Order order = (Order) xStream.fromXML("<order>" + "<date>"
                + "<time>1352913901530</time>" + "<timezone>America/Sao_Paulo</timezone>"
                + "</date>" + "</order>");
        assertNotNull(order.date);
        assertEquals(1352913901530l, order.date.getTimeInMillis());
    }
    
    @Test
    public void shouldBuildCalendarIgnoringFirstDate() {
		Converter converter = new ConstructorConverterBuilder(Order.class).withConstructor(Calendar.class).withAliases("date2");
        XStream xStream = new XStream();
        xStream.alias("order", Order.class);
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

}
