package net.thewesthill.protocol.netty.codec;

import java.io.Serializable;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MarshallingCodecTest {

  public static void main(String[] args) throws Exception {
    testString();
    testInteger();
    testCustomPojo();
    testMultipleObjectsInSameBuffer();
    System.out.println("all tests passed");
  }

  private static void testString() throws Exception {
    ByteBuf buf = Unpooled.buffer();
    new MarshallingEncoder().encode("Hello, Marshalling!", buf);
    System.out.println("  raw bytes (" + buf.readableBytes() + " total):");
    hexDump(buf);
    Object decoded = new MarshallingDecoder().decode(buf);
    assertEquals("Hello, Marshalling!", decoded);
    System.out.println("  [ok] String round-trip");
  }

  private static void hexDump(ByteBuf buf) {
    int readerIndex = buf.readerIndex();
    int writerIndex = buf.writerIndex();
    StringBuilder sb = new StringBuilder("    ");
    for (int i = readerIndex; i < writerIndex; i++) {
      sb.append(String.format("%02x", buf.getByte(i) & 0xff));
      if ((i - readerIndex + 1) % 16 == 0) {
        sb.append("\n    ");
      } else {
        sb.append(' ');
      }
    }
    System.out.println(sb);
    // 标注前4字节是长度字段
    int length = buf.getInt(readerIndex);
    System.out.println("    first 4 bytes as int (length field) = " + length);
  }

  private static void testInteger() throws Exception {
    ByteBuf buf = Unpooled.buffer();
    new MarshallingEncoder().encode(12345, buf);
    Object decoded = new MarshallingDecoder().decode(buf);
    assertEquals(12345, decoded);
    System.out.println("  [ok] Integer round-trip");
  }

  private static void testCustomPojo() throws Exception {
    ByteBuf buf = Unpooled.buffer();
    User user = new User("zhangsan", 20);
    new MarshallingEncoder().encode(user, buf);
    Object decoded = new MarshallingDecoder().decode(buf);
    assertEquals(user, decoded);
    System.out.println("  [ok] custom POJO round-trip");
  }

  private static void testMultipleObjectsInSameBuffer() throws Exception {
    ByteBuf buf = Unpooled.buffer();
    new MarshallingEncoder().encode("first", buf);
    new MarshallingEncoder().encode("second", buf);

    Object first = new MarshallingDecoder().decode(buf);
    Object second = new MarshallingDecoder().decode(buf);
    assertEquals("first", first);
    assertEquals("second", second);
    System.out.println("  [ok] multiple objects in one ByteBuf");
  }

  private static void assertEquals(Object expected, Object actual) {
    if (!expected.equals(actual)) {
      throw new AssertionError("expected=" + expected + " actual=" + actual);
    }
  }

  private static final class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final int age;

    User(String name, int age) {
      this.name = name;
      this.age = age;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof User)) return false;
      User u = (User) o;
      return age == u.age && name.equals(u.name);
    }

    @Override
    public int hashCode() {
      return name.hashCode() * 31 + age;
    }

    @Override
    public String toString() {
      return "User[name=" + name + ", age=" + age + "]";
    }
  }
}
