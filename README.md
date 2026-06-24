<picture>
  <img alt="netty-eg banner" src="https://placehold.co/640x160/1f2328/ffffff?text=netty-eg">
</picture>

[![Project Status: Active – The project is being actively developed.](https://www.repostatus.org/badges/latest/active.svg)](https://www.repostatus.org/#active)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Netty](https://img.shields.io/badge/Netty-4.2.1.Final-00C7B7?logo=netty&logoColor=white)](https://netty.io/)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)

**netty-eg** is a small, hands-on study project that walks through Java network programming from raw blocking I/O,
through native NIO, up to [Netty](https://netty.io/) — including the TCP sticking / half-packet problem and the codec (
decoder) patterns Netty provides to solve it.

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
    - [Non-blocking I/O (NIO)](#non-blocking-io-nio)
    - [Netty basics](#netty-basics)
    - [TCP sticking and half-packet](#tcp-sticking-and-half-packet)
    - [Netty decoders](#netty-decoders)
- [Asking questions and reporting issues](#asking-questions-and-reporting-issues)
- [Contributing](#contributing)
- [Additional resources](#additional-resources)
- [Changelog](#changelog)
- [License](#license)

# How it works

The examples are intentionally ordered so that each layer addresses a shortcoming of the previous one:

1. **BIO** — one-thread-per-connection servers. Easy to understand, but does not scale: each client holds a thread for
   the lifetime of the connection.
2. **NIO** — a single multiplexed thread handles many connections via `Selector`. Better scalability, but the code is
   verbose and easy to get wrong. The async variant demonstrates JDK AIO (`CompletionHandler`).
3. **Netty basics** — the same time server/client reimplemented on top of Netty's `EventLoopGroup` + `ChannelPipeline`.
   The boilerplate collapses; the intent of the code becomes readable.
4. **TCP sticking / half-packet** — when clients stream many small writes, TCP may coalesce them (sticking) or split a
   single logical message across segments (half-packet). The example shows a server that misbehaves without a decoder,
   then fixes it with `LineBasedFrameDecoder`.
5. **Netty decoders** — production-grade solutions to framing: `DelimiterBasedFrameDecoder` (split on `$_`) and
   `FixedLengthFrameDecoder` (fixed 20-byte frames).

# Project structure

```
src/main/java/net/thewesthill/example/
├── bio/
│   ├── sync/      # Blocking I/O time server + client
│   └── async/    # Pseudo-async BIO (thread pool) time server
├── nio/
│   ├── sync/     # JDK NIO multiplexer time server + client
│   └── async/    # JDK AIO (CompletionHandler) time server + client
└── netty/
    ├── eg/         # Basic Netty time server + client
    ├── sticking/  # TCP sticking/half-packet demo with LineBasedFrameDecoder
    └── decoder/    # Delimiter- and fixed-length frame decoders
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

Every example is a standalone `main` class. All server examples accept an optional port argument (default `8080`);
client examples target `127.0.0.1:8080` unless noted.

Run any example with `exec:java`:

```bash
./mvnw exec:java -Dexec.mainClass=<fully.qualified.ClassName>
```

## Blocking I/O (BIO)

| Class                                                  | Role                                  |
|--------------------------------------------------------|---------------------------------------|
| `net.thewesthill.example.bio.sync.BioSyncTimeServer`   | Synchronous blocking time server      |
| `net.thewesthill.example.bio.sync.BioSyncTimeClient`   | Matching blocking client              |
| `net.thewesthill.example.bio.async.BioAsyncTimeServer` | Pseudo-async BIO server (thread pool) |

```bash
./mvnw exec:java -Dexec.mainClass=net.thewesthill.example.bio.sync.BioSyncTimeServer
# in another shell
./mvnw exec:java -Dexec.mainClass=net.thewesthill.example.bio.sync.BioSyncTimeClient
```

## Non-blocking I/O (NIO)

| Class                                                        | Role                            |
|--------------------------------------------------------------|---------------------------------|
| `net.thewesthill.example.nio.sync.NioSyncTimeServer`         | Multiplexed NIO time server     |
| `net.thewesthill.example.nio.sync.NioSyncTimeClient`         | Matching NIO client             |
| `net.thewesthill.example.nio.async.NioAsyncTimeClient`       | JDK AIO client                  |
| `net.thewesthill.example.nio.async.NioAsyncTimeClientHandle` | AIO client handler (has `main`) |

## Netty basics

| Class                                              | Role                    |
|----------------------------------------------------|-------------------------|
| `net.thewesthill.example.netty.eg.NettyTimeServer` | Basic Netty time server |
| `net.thewesthill.example.netty.eg.NettyTimeClient` | Basic Netty time client |

```bash
./mvnw exec:java -Dexec.mainClass=net.thewesthill.example.netty.eg.NettyTimeServer
./mvnw exec:java -Dexec.mainClass=net.thewesthill.example.netty.eg.NettyTimeClient
```

## TCP sticking and half-packet

| Class                                                 | Role                                      |
|-------------------------------------------------------|-------------------------------------------|
| `net.thewesthill.example.netty.sticking.NSTimeServer` | Server with `LineBasedFrameDecoder(1024)` |
| `net.thewesthill.example.netty.sticking.NSTimeClient` | Client that triggers sticking behavior    |

## Netty decoders

| Class                                                         | Role                                        |
|---------------------------------------------------------------|---------------------------------------------|
| `net.thewesthill.example.netty.decoder.DelimiterEchoServer`   | Echo server splitting on the `$_` delimiter |
| `net.thewesthill.example.netty.decoder.DelimiterEchoClient`   | Matching echo client                        |
| `net.thewesthill.example.netty.decoder.FixedLengthEchoServer` | Echo server with fixed 20-byte frames       |

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
