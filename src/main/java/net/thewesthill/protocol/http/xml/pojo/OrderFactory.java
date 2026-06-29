package net.thewesthill.protocol.http.xml.pojo;

public class OrderFactory {
    
  public static Order create(long orderId) {
    Order order = new Order();
    order.setOrderNumber(orderId);
    order.setTotal(9999.999f);
    Address address = new Address();
    address.setCity("Zhuhai");
    address.setCountry("China");
    address.setPostCode("025520");
    address.setState("Guangdong");
    address.setStreet1("Big Way");
    order.setBillTo(address);
    Customer customer = new Customer();
    customer.setCustomMember(orderId);
    customer.setFirstName("Fan");
    customer.setLastName("Jonah");
    order.setCustomer(customer);
    order.setShipping(Shipping.INTERNATIONAL_MAIL);
    order.setShipTo(address);
    return order;
  }
}
