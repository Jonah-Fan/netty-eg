package net.thewesthill.protocol.http.xml;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import lombok.extern.slf4j.Slf4j;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

import net.thewesthill.protocol.http.xml.pojo.Order;
import net.thewesthill.protocol.http.xml.pojo.OrderFactory;

@Slf4j
public class TestOrder {

  private IBindingFactory factory = null;
  private StringWriter writer = null;
  private StringReader reader = null;
  private static final String CHARSET_NAME = "UTF-8";

  private String encode2Xml(Order order) throws JiBXException, IOException {
    factory = BindingDirectory.getFactory(Order.class);
    writer = new StringWriter();
    IMarshallingContext ctx = factory.createMarshallingContext();
    ctx.setIndent(2);
    ctx.marshalDocument(order, CHARSET_NAME, null, writer);
    String xmlStr = writer.toString();
    writer.close();
    log.info("{}", xmlStr);
    return xmlStr;
  }

  private Order decode2Order(String xmlStr) throws JiBXException {
    reader = new StringReader(xmlStr);
    IUnmarshallingContext ctx = factory.createUnmarshallingContext();
    Order order = (Order) ctx.unmarshalDocument(reader);
    return order;
  }

  public static void main(String[] args) throws JiBXException, IOException {
    TestOrder test = new TestOrder();
    Order order = OrderFactory.create(999);
    String body = test.encode2Xml(order);
    Order decodeOrder = test.decode2Order(body);
    log.info("{}", decodeOrder);
  }
}
