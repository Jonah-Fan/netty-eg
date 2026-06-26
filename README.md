<picture>
  <img alt="netty-eg banner" src="https://placehold.co/640x160/1f2328/ffffff?text=netty-eg">
</picture>

[![Project Status: Active – The project is being actively developed.](https://www.repostatus.org/badges/latest/active.svg)](https://www.repostatus.org/#active)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Netty](https://img.shields.io/badge/Netty-4.2.1.Final-00C7B7?logo=netty&logoColor=white)](https://netty.io/)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)

**netty-eg** is a small, hands-on study project that walks through Java network programming from raw blocking I/O,
through native NIO and JDK AIO, up to [Netty](https://netty.io/) — including the TCP sticking / half-packet problem,
the frame decoders Netty provides to solve it, and several binary codecs (MessagePack, Protobuf, JBoss Marshalling)
on top.

The repository is a learning aid, not a production framework. Each example is a self-contained `main` class you can read
end-to-end and run with a single command.

> [!IMPORTANT]
> This README is a quick orientation. For authoritative reference material on Netty internals, handlers, and codecs,
> consult the [official Netty documentation](https://netty.io/wiki/) and
> the [Netty User Guide](https://netty.io/wiki/user-guide-for-4.x.html). Source code is the source of truth for these
> examples.

# Table of contents

- [How it works](#how-it-works)
- [Project structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Building from source](#building-from-source)
- [Running the examples](#running-the-examples)
    - [Blocking I/O (BIO)](#blocking-io-bio)
    - [Pseudo-async I/O (PIO)](#pseudo-async-io-pio)
    - [Non-blocking I/O (NIO)](#non-blocking-io-nio)
    - [Asynchronous I/O (AIO)](#asynchronous-io-aio)
    - [Netty basics](#netty-basics)
    - [TCP sticking and half-packet](#tcp-sticking-and-half-packet)
    - [Frame decoders](#frame-decoders)
    - [MessagePack codec](#messagepack-codec)
    - [Protobuf codec](#protobuf-codec)
    - [JBoss Marshalling codec](#jboss-marshalling-codec)
    - [JDK serialization benchmark](#jdk-serialization-benchmark)
    - [HTTP file server](#http-file-server)
- [Asking questions and reporting issues](#asking-questions-and-reporting-issues)
- [Contributing](#contributing)
- [Additional resources](#additional-resources)
- [Changelog](#changelog)
- [License](#license)
- [Contact](#contact)

# How it works

The examples are intentionally ordered so that each layer addresses a shortcoming of the previous one:

1. **BIO** — one-thread-per-connection servers. Easy to understand, but does not scale: each client holds a thread for
   the lifetime of the connection.
2. **PIO** — the same BIO server with a bounded thread pool in front of `accept()`. Reuses threads across connections
   but still blocks on each socket read.
3. **NIO** — a single multiplexed thread handles many connections via `Selector`. Better scalability, but the code is
   verbose and easy to get wrong.
4. **AIO** — JDK asynchronous channel API driven by `CompletionHandler` callbacks. Truly non-blocking on platforms
   that support it.
5. **Netty basics** — the same time server/client reimplemented on top of Netty's `EventLoopGroup` + `ChannelPipeline`.
   The boilerplate collapses; the intent of the code becomes readable.
6. **TCP sticking / half-packet** — when clients stream many small writes, TCP may coalesce them (sticking) or split a
   single logical message across segments (half-packet). The `frame/fault` handlers read raw `ByteBuf` and exhibit the
   problem; `frame/correct` prepends a `LineBasedFrameDecoder` to fix it.
7. **Frame decoders** — production-grade solutions to framing: `DelimiterBasedFrameDecoder` (split on `$_`) and
   `FixedLengthFrameDecoder` (fixed 20-byte frames).
8. **Codecs** — binary object serialization on top of a length-prefixed frame:
    - **MessagePack** — `jackson-dataformat-msgpack` with `LengthFieldPrepender` + `LengthFieldBasedFrameDecoder`.
    - **Protobuf** — `ProtobufEncoder`/`ProtobufDecoder` with `ProtobufVarint32LengthFieldPrepender`/
      `ProtobufVarint32FrameDecoder`.
    - **JBoss Marshalling** — `MarshallingEncoder`/`MarshallingDecoder` built on the `serial` provider.
9. **Protocol** — an (in-progress) HTTP file server skeleton showing how to stack `HttpRequestDecoder` +
   `HttpObjectAggregator` + `ChunkedWriteHandler`.

# Project structure

```
src/main/java/net/thewesthill/
├── bio/              # Blocking I/O time server + client
├── pio/              # Pseudo-async BIO (thread pool) time server
├── nio/              # JDK NIO multiplexer time server + client
├── aio/              # JDK AIO (CompletionHandler) time server + client
├── basic/            # Basic Netty time server + client
├── frame/
│   ├── fault/          # Raw-ByteBuf handlers that exhibit TCP sticking
│   ├── correct/        # Same handlers behind a LineBasedFrameDecoder
│   ├── delimiter/      # DelimiterBasedFrameDecoder ($_ delimiter) echo
│   └── fixedlen/       # FixedLengthFrameDecoder (20-byte frames) echo
├── codec/
│   ├── msgpack/        # MessagePack codec with length-field framing
│   ├── protobuf/       # Protobuf codec with Varint32 length framing
│   ├── marshalling/    # JBoss Marshalling (serial provider) codec
│   ├── pojo/           # Generated protobuf message classes
│   └── serializable/   # JDK Serializable vs. hand-rolled byte buffer
└── protocol/
    └── http/           # HTTP file server skeleton (work in progress)
```

# Prerequisites

- **JDK 17 or later** — the project targets Java 17 (`maven.compiler.source`/`target`).
- **Maven 3.9+** — or simply use the bundled Maven wrapper (`./mvnw` on Unix, `mvnw.cmd` on Windows), which downloads
  the correct Maven version on first use.

# Building from source

Clone the repository and build with the wrapper:

```bash
git clone https://github.com/Jonah-Fan/netty-eg.git
cd netty-eg
./mvnw clean package
```

> [!TIP]
> On Windows, replace `./mvnw` with `mvnw.cmd` throughout.

To compile without running tests:

```bash
./mvnw clean compile
```

# Running the examples

Every example is a standalone `main` class. Most servers accept an optional port argument (default `8080`); a few
hardcode `8080` for brevity. Clients target `127.0.0.1:8080` unless noted.

Run any example with `exec:java`:

```bash
./mvnw exec:java -Dexec.mainClass=<fully.qualified.ClassName>
```

## Blocking I/O (BIO)

| Class                            | Role                              |
|----------------------------------|-----------------------------------|
| `net.thewesthill.bio.TimeServer` | Synchronous blocking time server  |
| `net.thewesthill.bio.TimeClient` | Matching blocking client          |

```bash
./mvnw exec:java -Dexec.mainClass=net.thewesthill.bio.TimeServer
# in another shell
./mvnw exec:java -Dexec.mainClass=net.thewesthill.bio.TimeClient
```

## Pseudo-async I/O (PIO)

The BIO accept loop dispatching into a bounded `ThreadPoolExecutor` instead of spawning a thread per connection.
Reuses the `bio.TimeServer.TimeServerHandle` runnable.

| Class                            | Role                                          |
|----------------------------------|-----------------------------------------------|
| `net.thewesthill.pio.TimeServer` | BIO server backed by a `ThreadPoolExecutor` |

## Non-blocking I/O (NIO)

| Class                                     | Role                                   |
|-------------------------------------------|----------------------------------------|
| `net.thewesthill.nio.TimeServer`          | Multiplexed NIO time server (entry)    |
| `net.thewesthill.nio.MultiplexerTimeServer` | `Selector` loop handling accept/read |
| `net.thewesthill.nio.TimeClient`          | Matching NIO client                    |

## Asynchronous I/O (AIO)

JDK `AsynchronousServerSocketChannel` driven by `CompletionHandler` callbacks for accept and read.

| Class                                     | Role                                  |
|-------------------------------------------|---------------------------------------|
| `net.thewesthill.aio.TimeServer`          | AIO time server (entry)               |
| `net.thewesthill.aio.AsyncTimeServerHandler` | Accept loop + `AcceptCompletionHandler` |
| `net.thewesthill.aio.TimeClient`          | AIO client (entry)                    |
| `net.thewesthill.aio.AsyncTimeClientHandler` | Connect + `ReadCompletionHandler`     |

## Netty basics

| Class                              | Role                    |
|------------------------------------|-------------------------|
| `net.thewesthill.basic.TimeServer` | Basic Netty time server |
| `net.thewesthill.basic.TimeClient` | Basic Netty time client |

```bash
./mvnw exec:java -Dexec.mainClass=net.thewesthill.basic.TimeServer
./mvnw exec:java -Dexec.mainClass=net.thewesthill.basic.TimeClient
```

## TCP sticking and half-packet

The client fires 100 `QUERY TIME ORDER` messages on channel active. Without a frame decoder the server sees them
coalesced into fewer reads; with `LineBasedFrameDecoder(1024)` each line is delivered as a separate `ByteBuf`.

| Class                                     | Role                                              |
|-------------------------------------------|---------------------------------------------------|
| `net.thewesthill.frame.fault.TimeServerHandler` | Reads raw `ByteBuf`; exhibits sticking behavior |
| `net.thewesthill.frame.fault.TimeClientHandler` | Sends 100 messages to trigger sticking          |
| `net.thewesthill.frame.correct.TimeServer` | Same handler behind `LineBasedFrameDecoder(1024)` |
| `net.thewesthill.frame.correct.TimeClient` | Adds `LineBasedFrameDecoder` + `StringDecoder`   |

To see the fault behavior, comment out the `LineBasedFrameDecoder` line in `frame.correct.TimeServer` / `TimeClient`
and rerun.

## Frame decoders

| Class                                          | Role                                         |
|------------------------------------------------|----------------------------------------------|
| `net.thewesthill.frame.delimiter.EchoServer`  | Echo server splitting on the `$_` delimiter |
| `net.thewesthill.frame.delimiter.EchoClient`  | Matching echo client                         |
| `net.thewesthill.frame.fixedlen.EchoServer`   | Echo server with fixed 20-byte frames        |

## MessagePack codec

An echo server/client pair that serializes `UserInfo` with MessagePack and frames it with a 2-byte length prefix
so the decoder always receives a complete message.

| Class                                       | Role                                                       |
|---------------------------------------------|------------------------------------------------------------|
| `net.thewesthill.codec.msgpack.EchoServer`  | Echo server with `LengthFieldBasedFrameDecoder` + msgpack  |
| `net.thewesthill.codec.msgpack.EchoClient`  | Matching client; sends N `UserInfo` objects on connect    |

```bash
./mvnw exec:java -Dexec.mainClass=net.thewesthill.codec.msgpack.EchoServer
# in another shell
./mvnw exec:java -Dexec.mainClass=net.thewesthill.codec.msgpack.EchoClient
```

## Protobuf codec

A subscribe-request server/client pair using `SubscribeReqProto.SubscribeReq` /
`SubscribeRespProto.SubscribeResp` (generated classes live under `codec.pojo`). Varint32 length framing keeps each
protobuf message self-delimited on the wire.

| Class                                              | Role                                            |
|----------------------------------------------------|-------------------------------------------------|
| `net.thewesthill.codec.protobuf.SubReqServer`     | Server with `ProtobufVarint32FrameDecoder` etc. |
| `net.thewesthill.codec.protobuf.SubReqClient`     | Matching client                                 |
| `net.thewesthill.codec.protobuf.TestSubscribeReqProto` | Standalone encode/decode round-trip test  |

```bash
./mvnw exec:java -Dexec.mainClass=net.thewesthill.codec.protobuf.SubReqServer
# in another shell
./mvnw exec:java -Dexec.mainClass=net.thewesthill.codec.protobuf.SubReqClient
```

## JBoss Marshalling codec

Same subscribe-request flow as the protobuf example, but with JBoss Marshalling (`serial` provider, version 5) in
place of the protobuf encoder/decoder. Reuses the protobuf handlers since `SubscribeReq` is a regular `Serializable`
Java object.

| Class                                                | Role                                                |
|------------------------------------------------------|-----------------------------------------------------|
| `net.thewesthill.codec.marshalling.SubReqServer`    | Server with `MarshallingDecoder`/`MarshallingEncoder` |
| `net.thewesthill.codec.marshalling.SubReqClient`    | Matching client                                     |
| `net.thewesthill.codec.marshalling.MarshallingCodecFactory` | Builds the encoder/decoder from the `serial` factory |

## JDK serialization benchmark

Two standalone `main` classes comparing JDK `ObjectOutputStream` against a hand-rolled `ByteBuffer` codec on the
same `UserInfo` POJO. `PerformTestUserInfo` loops 1,000,000 times to measure throughput.

| Class                                              | Role                                              |
|----------------------------------------------------|---------------------------------------------------|
| `net.thewesthill.codec.serializable.TestUserInfo`     | One-shot size comparison                      |
| `net.thewesthill.codec.serializable.PerformTestUserInfo` | 1M-iteration timing benchmark               |

## HTTP file server

A scaffold stacking `HttpRequestDecoder` + `HttpObjectAggregator` + `HttpResponseEncoder` + `ChunkedWriteHandler`.
The handler is a stub — file listing and serving are not implemented yet, so this example is not runnable as-is.

| Class                                          | Role                          |
|------------------------------------------------|-------------------------------|
| `net.thewesthill.protocol.http.HttpFileServer` | HTTP file server skeleton     |
| `net.thewesthill.protocol.http.HttpFileServerHandler` | Stub handler (TODO)    |

# Asking questions and reporting issues

Use the [GitHub Issues](https://github.com/Jonah-Fan/netty-eg/issues) tracker for bug reports, suggestions, or questions
about specific examples. When reporting an issue, include:

- The example's fully-qualified class name
- JDK version (`java -version`)
- Steps to reproduce and the expected vs. actual behavior

# Contributing

Contributions are welcome. Please open an issue first to discuss any non-trivial change, then submit a pull request
against `master`. Keep commits focused and follow the
existing [conventional commit](https://www.conventionalcommits.org/) style (`feat:`, `fix:`, `refactor:`, `docs:`, …).

# Additional resources

- [Netty — User Guide for 4.x](https://netty.io/wiki/user-guide-for-4.x.html)
- [Netty — Wiki](https://netty.io/wiki/)
- [Netty source on GitHub](https://github.com/netty/netty)

# Changelog

This project does not maintain a separate changelog file. Refer to
the [commit history](https://github.com/Jonah-Fan/netty-eg/commits/master) for the full record of changes.

# License

No license has been declared for this repository yet. Until one is added, default copyright terms apply and the source
is shared here for study purposes only. If you intend to reuse the code, please open an issue to discuss licensing
first.

# Contact

| | |
| :-- | :-- |
| **Name**  | Jonah Fan                |
| **Email** | <jonah-fan@outlook.com>  |

For project-related discussions, please prefer the
[GitHub Issues](https://github.com/Jonah-Fan/netty-eg/issues) tracker; use email for off-thread or private
inquiries.
