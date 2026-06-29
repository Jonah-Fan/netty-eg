package net.thewesthill.protocol.http.xml.pojo;

import java.util.List;

public class Customer {

  private long customNumber;

  private String firstName;

  private String lastName;

  private List<String> middleName;

  public long getCustomMember() {
    return customNumber;
  }

  public void setCustomMember(long customNumber) {
    this.customNumber = customNumber;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public List<String> getMiddleName() {
    return middleName;
  }

  public void setMiddleName(List<String> middleName) {
    this.middleName = middleName;
  }

  @Override
  public String toString() {
    return "Customer [customerNumber="
        + customNumber
        + ", firstName="
        + firstName
        + ", lastName="
        + lastName
        + ", middleNames="
        + middleName
        + "]";
  }
}
