<picture>
  <img alt="netty-eg banner" src="https://placehold.co/640x160/1f2328/ffffff?text=netty-eg">
</picture>

[![Project Status: Active – The project is being actively developed.](https://www.repostatus.org/badges/latest/active.svg)](https://www.repostatus.org/#active)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Netty](https://img.shields.io/badge/Netty-4.2.1.Final-00C7B7?logo=netty&logoColor=white)](https://netty.io/)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)

**netty-eg** is a hands-on study project that walks through Java network programming — raw blocking I/O, native NIO,
JDK AIO, and [Netty](https://netty.io/) — including the TCP sticking / half-packet problem, the frame decoders Netty
provides to solve it, and several binary codecs (MessagePack, Protobuf, JBoss Marshalling) on top.

Each example is a self-contained `main` class you can read end-to-end and run with a single command. The repository is a
learning aid, not a production framework.

> [!IMPORTANT]
> This README is a quick orientation. For authoritative reference material on Netty internals, handlers, and codecs,
> consult the [official Netty documentation](https://netty.io/wiki/) and
> the [Netty User Guide](https://netty.io/wiki/user-guide-for-4.x.html). Source code is the source of truth for these
> examples.

## How it works

Each layer addresses a shortcoming of the previous one:

1. **BIO** — one thread per connection. Simple, but does not scale.
2. **PIO** — same BIO server fronted by a bounded `ThreadPoolExecutor`. Reuses threads but still blocks on read.
3. **NIO** — one multiplexed thread via `Selector`. Better scalability, verbose code.
4. **AIO** — JDK `AsynchronousServerSocketChannel` driven by `CompletionHandler` callbacks.
5. **Netty basics** — same time server/client on `EventLoopGroup` + `ChannelPipeline`. Boilerplate collapses.
6. **Frame decoders** — `LineBasedFrameDecoder`, `DelimiterBasedFrameDecoder` (`$_`), `FixedLengthFrameDecoder` (20 B).
7. **Codecs** — MessagePack, Protobuf, JBoss Marshalling, each on top of a length-prefixed frame.
8. **Protocol** — HTTP file server skeleton stacking `HttpRequestDecoder` + `HttpObjectAggregator` + `ChunkedWriteHandler`.

## Project structure

```
src/main/java/net/thewesthill/
├── bio/              # Blocking I/O time server + client
├── pio/              # Pseudo-async BIO (thread pool) time server
├── nio/              # JDK NIO multiplexer time server + client
├── aio/              # JDK AIO (CompletionHandler) time server + client
├── basic/            # Basic Netty time server + client
├── frame/
│   ├── fault/         # Raw-ByteBuf handlers that exhibit TCP sticking
│   ├── correct/      # Same handlers behind a LineBasedFrameDecoder
│   ├── delimiter/    # DelimiterBasedFrameDecoder ($_ delimiter) echo
│   └── fixedlen/     # FixedLengthFrameDecoder (20-byte frames) echo
├── codec/
│   ├── msgpack/       # MessagePack codec with length-field framing
│   ├── protobuf/      # Protobuf codec with Varint32 length framing
│   ├── marshalling/   # JBoss Marshalling (serial provider) codec
│   ├── pojo/          # Generated protobuf message classes
│   └── serializable/  # JDK Serializable vs. hand-rolled byte buffer
└── protocol/
    └── http/          # HTTP file server skeleton (work in progress)
```

## Prerequisites

- **JDK 17 or later** — targets Java 17 (`maven.compiler.source`/`target`).
- **Maven 3.9+** — or use the bundled wrapper (`./mvnw` on Unix, `mvnw.cmd` on Windows).

## Building from source

```bash
git clone https://github.com/Jonah-Fan/netty-eg.git
cd netty-eg
./mvnw clean package
```

> [!TIP]
> On Windows, replace `./mvnw` with `mvnw.cmd` throughout.

## Running the examples

Every example is a standalone `main` class. Servers accept an optional port argument (default `8080`); clients target
`127.0.0.1:8080` unless noted.

```bash
./mvnw exec:java -Dexec.mainClass=<fully.qualified.ClassName>
```

### BIO

- `bio.TimeServer` — synchronous blocking time server
- `bio.TimeClient` — matching blocking client

### PIO

- `pio.TimeServer` — BIO accept loop dispatching into a `ThreadPoolExecutor` (reuses `bio.TimeServer.TimeServerHandle`)

### NIO

- `nio.TimeServer` — entry point; starts the multiplexer on a dedicated thread
- `nio.MultiplexerTimeServer` — `Selector` loop handling accept + read
- `nio.TimeClient` — matching NIO client

### AIO

JDK `AsynchronousServerSocketChannel` driven by `CompletionHandler` callbacks for accept and read.

- `aio.TimeServer` — server entry
- `aio.AsyncTimeServerHandler` — accept loop + `AcceptCompletionHandler`
- `aio.TimeClient` — client entry
- `aio.AsyncTimeClientHandler` — connect + `ReadCompletionHandler`

### Netty basics

- `basic.TimeServer` — basic Netty time server
- `basic.TimeClient` — basic Netty time client

### TCP sticking and half-packet

The client fires 100 `QUERY TIME ORDER` messages on channel active. Without a frame decoder the server sees them
coalesced; with `LineBasedFrameDecoder(1024)` each line arrives as a separate `ByteBuf`.

- `frame.fault.TimeServerHandler` — reads raw `ByteBuf`, exhibits sticking
- `frame.fault.TimeClientHandler` — sends 100 messages to trigger sticking
- `frame.correct.TimeServer` — same handler behind `LineBasedFrameDecoder(1024)`
- `frame.correct.TimeClient` — adds `LineBasedFrameDecoder` + `StringDecoder`

To see the fault behavior, comment out the `LineBasedFrameDecoder` line in `frame.correct.TimeServer` / `TimeClient`
and rerun.

### Frame decoders

- `frame.delimiter.EchoServer` — echo server splitting on the `$_` delimiter
- `frame.delimiter.EchoClient` — matching echo client
- `frame.fixedlen.EchoServer` — echo server with fixed 20-byte frames

### MessagePack codec

Echo server/client pair that serializes `UserInfo` with MessagePack and frames it with a 2-byte length prefix.

- `codec.msgpack.EchoServer` — `LengthFieldBasedFrameDecoder` + msgpack decoder
- `codec.msgpack.EchoClient` — sends N `UserInfo` objects on connect
- `codec.msgpack.MsgPackEncoder` / `MsgPackDecoder` — codec pair

### Protobuf codec

Subscribe-request server/client using `SubscribeReqProto.SubscribeReq` / `SubscribeRespProto.SubscribeResp` (generated
classes live under `codec.pojo`). Varint32 length framing keeps each protobuf message self-delimited.

- `codec.protobuf.SubReqServer` — `ProtobufVarint32FrameDecoder` + `ProtobufDecoder`
- `codec.protobuf.SubReqClient` — matching client
- `codec.protobuf.TestSubscribeReqProto` — standalone encode/decode round-trip test

### JBoss Marshalling codec

Same subscribe-request flow as the protobuf example, but with JBoss Marshalling (`serial` provider, version 5).
Reuses the protobuf handlers since `SubscribeReq` is a regular `Serializable` Java object.

- `codec.marshalling.SubReqServer` — `MarshallingDecoder` / `MarshallingEncoder`
- `codec.marshalling.SubReqClient` — matching client
- `codec.marshalling.MarshallingCodecFactory` — builds the encoder/decoder from the `serial` factory

### JDK serialization benchmark

Standalone `main` classes comparing JDK `ObjectOutputStream` against a hand-rolled `ByteBuffer` codec on the same
`UserInfo` POJO. `PerformTestUserInfo` loops 1,000,000 times to measure throughput.

- `codec.serializable.TestUserInfo` — one-shot size comparison
- `codec.serializable.PerformTestUserInfo` — 1M-iteration timing benchmark

### HTTP file server

Scaffold stacking `HttpRequestDecoder` + `HttpObjectAggregator` + `HttpResponseEncoder` + `ChunkedWriteHandler`. The
handler is a stub — file listing and serving are not implemented yet, so this example is not runnable as-is.

- `protocol.http.HttpFileServer` — HTTP file server skeleton
- `protocol.http.HttpFileServerHandler` — stub handler (TODO)

## Asking questions and reporting issues

Use the [GitHub Issues](https://github.com/Jonah-Fan/netty-eg/issues) tracker for bug reports, suggestions, or
questions about specific examples. When reporting an issue, include:

- The example's fully-qualified class name
- JDK version (`java -version`)
- Steps to reproduce and the expected vs. actual behavior

## Contributing

Contributions are welcome. Please open an issue first to discuss any non-trivial change, then submit a pull request
against `master`. Keep commits focused and follow the
existing [conventional commit](https://www.conventionalcommits.org/) style (`feat:`, `fix:`, `refactor:`, `docs:`, …).

## Additional resources

- [Netty — User Guide for 4.x](https://netty.io/wiki/user-guide-for-4.x.html)
- [Netty — Wiki](https://netty.io/wiki/)
- [Netty source on GitHub](https://github.com/netty/netty)

## Changelog

This project does not maintain a separate changelog file. Refer to
the [commit history](https://github.com/Jonah-Fan/netty-eg/commits/master) for the full record of changes.

## License

No license has been declared for this repository yet. Until one is added, default copyright terms apply and the source
is shared here for study purposes only. If you intend to reuse the code, please open an issue to discuss licensing
first.

## Contact

| | |
| :-- | :-- |
| **Name**  | Jonah Fan                |
| **Email** | <jonah-fan@outlook.com>  |

For project-related discussions, please prefer the
[GitHub Issues](https://github.com/Jonah-Fan/netty-eg/issues) tracker; use email for off-thread or private inquiries.
