package net.thewesthill.protobuf;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestSubscribeReqProto {

  private static byte[] encode(SubscribeReqProto.SubscribeReq req) {
    return req.toByteArray();
  }

  private static SubscribeReqProto.SubscribeReq decode(byte[] body)
      throws InvalidProtocolBufferException {
    return SubscribeReqProto.SubscribeReq.parseFrom(body);
  }

  private static SubscribeReqProto.SubscribeReq createSubscribeReq() {
    SubscribeReqProto.SubscribeReq.Builder builder = SubscribeReqProto.SubscribeReq.newBuilder();
    builder.setSubReqID(1);
    builder.setUserName("Jonah");
    builder.setProductName("Netty Book");
    List<String> address = new ArrayList<>();
    address.add("BeiJing LiuLiChang");
    address.add("ShenZhen HongShuLin");
    builder.addAllAddress(address);
    return builder.build();
  }

  public static void main(String[] args) throws InvalidProtocolBufferException {
    SubscribeReqProto.SubscribeReq req = createSubscribeReq();
    log.info("Begin encode : {}", req.toString());
    SubscribeReqProto.SubscribeReq req2 = decode(encode(req));
    log.info("After decode : {}", req2.toString());
    log.info("Assert equal : --> {}", req2.equals(req));
  }
}
