package net.thewesthill.example.netty.codec;

import java.io.Serial;
import java.io.Serializable;
import java.nio.ByteBuffer;

public class UserInfo implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String userName;

  private int userId;

  public UserInfo buildUserName(String userName) {
    this.userName = userName;
    return this;
  }

  public UserInfo buildUserId(int userId) {
    this.userId = userId;
    return this;
  }

  public final String getUserName() {
    return userName;
  }

  public final void serUserName(String userName) {
    this.userName = userName;
  }

  public final int getUserId() {
    return userId;
  }

  public final void setUserId(int userId) {
    this.userId = userId;
  }

  public byte[] codeC() {
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    byte[] val = this.userName.getBytes();
    buffer.putInt(val.length);
    buffer.put(val);
    buffer.putInt(this.userId);
    buffer.flip();
    val = null;
    byte[] result = new byte[buffer.remaining()];
    buffer.get(result);
    return result;
  }

  public byte[] codeC(ByteBuffer buffer) {
    buffer.clear();
    byte[] val = this.userName.getBytes();
    buffer.putInt(val.length);
    buffer.put(val);
    buffer.putInt(userId);
    buffer.flip();
    val = null;
    byte[] res = new byte[buffer.remaining()];
    buffer.get(res);
    return res;
  }
}
