<div align="center">

<img src="https://github.com/Pulse-MC/PulseNet/blob/main/assets/banner.png?raw=true" alt="PulseNet Banner" width="100%">

# PulseNet
### The Heartbeat of High-Performance Networking - Fabric Edition

**PulseNet** is a Fabric mod that brings high-performance packet batching to your Minecraft server. By replacing standard per-packet network flushes with intelligent batching, PulseNet reduces system call overhead and CPU usage while maintaining low-latency delivery of critical packets.

[![Website](https://img.shields.io/badge/Website-pulsemc.dev-333333?style=for-the-badge&logo=google-chrome&logoColor=007bff)](https://pulsemc.dev)
[![Discord](https://img.shields.io/discord/1458504248215212145.svg?label=&logo=discord&logoColor=ffffff&color=403e3e&labelColor=5865F2&style=for-the-badge)](https://dsc.gg/Pulse-MC)

</div>

## Project Overview

PulseNet is a Fabric mod that replaces Minecraft's default per-packet network flushing with a smart batching system. Outgoing packets are grouped and flushed together, drastically reducing kernel syscall overhead and event loop task scheduling while keeping combat, chat, and other latency-sensitive packets instant. Designed to work seamlessly alongside other Fabric mods via Mixin compatibility.

---

## Installation

PulseNet is a server-side Fabric mod. Drop it into your `mods/` folder alongside Fabric API.

**Requirements:**
* [Fabric Loader](https://fabricmc.net/) 0.18.5+
* [Fabric API](https://modrinth.com/mod/fabric-api)
* Minecraft 26.1+
* Java 25+

* Latest Builds: [https://modrinth.com/mod/pulsenet](https://modrinth.com/mod/pulsenet)

---

## Features

| Feature | Description |
| :--- | :--- |
| **Smart Packet Batching** | Groups outgoing packets within a tick and flushes them in a single kernel syscall instead of one per packet. Supports three modes: `smart_execution`, `strict_tick`, and `interval`. |
| **Write Queue** | Replaces per-packet event loop task submissions with a single batched write task, eliminating lambda allocations and cross-thread scheduling overhead. |
| **Packet Coalescing** | Bundles similar low-priority packets (particles, sounds) into `BundlePacket`s, reducing the client's received packet count. |
| **Explosion Optimization** | Detects mass block changes from explosions and replaces individual block update packets with a full chunk resend when a configurable threshold is exceeded. |
| **Packet Classification** | Automatically classifies packets as Critical, Instant, Chat, Ignored, or Coalesce — ensuring latency-sensitive packets (keepalive, disconnect, combat) always bypass the buffer. Fabric infrastructure packets (`minecraft:register`, `minecraft:unregister`) are handled at the Connection level to prevent mod handshake failures. |
| **Real-Time Metrics** | Built-in metrics system tracking logical/physical PPS, bandwidth, CPU usage, memory savings, and write queue efficiency. Viewable via commands or an in-game boss bar. |
| **Mixin Compatibility** | Uses a `@WrapOperation` approach that runs *after* all other mixins (Polymer, server-side translations, etc.), ensuring full compatibility with the Fabric mod ecosystem. |
| **Hot-Reloadable Config** | All settings are configurable via `pulse.properties` and in-game commands (`/pulse config`, `/pulse reload`) with no restart required. |

---

## Commands

| Command                                    | Description                                             |
|:-------------------------------------------|:--------------------------------------------------------|
| `/pulse`                                   | Displays the current PulseNet version.                  |
| `/pulse reload`                            | Reloads the configuration from disk.                    |
| `/pulse config`                            | View and modify all configuration settings in-game.     |
| `/pulse netstats [network\|cpu\|ram\|all]` | Displays real-time network, CPU, and memory statistics. |
| `/pulse netstats bar`                      | Toggles the metrics boss bar overlay.                   |
| `/pulse netstats reset`                    | Resets all metrics counters to zero.                    |
| `/pulse packetNames`                       | Lists all observed packet class names and channel IDs.  |
| `/pulse packetNames classes`               | Lists only observed packet class names.                 |
| `/pulse packetNames channels`              | Lists only observed custom payload channel IDs.         |
| `/pulse packetNames reset`                 | Clears the observed packet name lists.                  |

### Config Commands
The following commands can be used to adjust configurable settings in the `pulse.properties` file without a server reboot (Use `/pulse reload` after running these commands to hotswap changes). These commands can be suffixed with a value to set the setting, or used without a value to view the current setting.
* `/pulse config batchingEnabled` Master toggle for the smart packet batching system. When true, outgoing packets are buffered and flushed in batches instead of individually. (default: true)
* `/pulse config batchingMode` Batching mode: smart_execution (flush on tick + limits), strict_tick (flush only on tick end), interval (flush on a timer). (default: smart_execution)
* `/pulse config batchingMaxBatchSize` Maximum number of packets to buffer before forcing a flush. (default: 1024)
* `/pulse config batchingMaxBatchBytes` Maximum bytes to buffer before forcing a flush. (default: 32000)
* `/pulse config batchingFlushInterval` Interval in milliseconds for the INTERVAL batching mode. (default: 25)
* `/pulse config batchingSafetyMarginBytes` Safety margin in bytes subtracted from the max batch bytes limit to prevent overflow. (default: 64)
* `/pulse config batchingInstantPackets` List of packet class names that should flush the buffer immediately when sent. (default: ClientboundHurtAnimationPacket, ClientboundDamageEventPacket, ClientboundBlockEntityDataPacket)
* `/pulse config batchingIgnoredPackets` List of packet class names that should always bypass the buffer entirely. (default: [])
* `/pulse config batchingInstantChannels` List of plugin channel IDs (e.g. `axiom:hello`) whose custom payload packets should flush the buffer immediately when sent. Use `/pulse packetNames channels` to discover channel IDs. (default: [])
* `/pulse config batchingIgnoredChannels` List of plugin channel IDs whose custom payload packets should always bypass the buffer entirely. Use `/pulse packetNames channels` to discover channel IDs. Note: `minecraft:register` and `minecraft:unregister` are handled at the Connection level and always bypass the buffer regardless of this list. (default: [])
* `/pulse config batchingChatPacketsBypass` When true, chat-related packets bypass the buffer for instant delivery. (default: true)
* `/pulse config batchingOffThreadBypass` When true, packets sent from off the server thread bypass the buffer. (default: true)
* `/pulse config batchingWriteQueue` When true, buffered packets are queued and written in a single event loop task instead of calling Connection.send() per packet, eliminating per-packet lambda allocations and cross-thread task scheduling. (default: true)
* `/pulse config batchingPacketCoalescing` When true, packets in the coalesce list are bundled together into BundlePackets on flush, reducing the client's received packet count. (default: true)
* `/pulse config batchingCoalescePackets` List of packet class names to coalesce into BundlePackets (e.g. particles, sounds). Supports inner classes with dot syntax (Foo.Bar). (default: ClientboundLevelParticlesPacket, ClientboundSoundPacket, ClientboundSoundEntityPacket)
* `/pulse config batchingCoalesceBundleLimit` Maximum number of sub-packets per BundlePacket. Vanilla client enforces a hard limit of 4096. (default: 4000)
* `/pulse config optExplosionsEnabled` When true, block updates from explosions are grouped by chunk and replaced with full chunk resends if they exceed the threshold. (default: true)
* `/pulse config optExplosionsBlockChangeThreshold` Number of block changes in a single chunk required to trigger a full chunk resend instead of individual updates. (default: 512)
* `/pulse config optExplosionsLogOptimizations` When true, logs a message each time the explosion optimization replaces block updates with a chunk resend. (default: false)
* `/pulse config metricsEnabled` When true, the metrics collection system is active and computes network statistics. (default: true)
* `/pulse config metricsUpdateInterval` How often (in seconds) the metrics system recomputes derived statistics. (default: 1)
* `/pulse config metricsModuleNetwork` Enables the network metrics module (PPS, bandwidth). (default: true)
* `/pulse config metricsModuleCpu` Enables the CPU metrics module (process CPU usage estimation). (default: true)
* `/pulse config metricsModuleMemory` Enables the memory metrics module (saved allocation estimation). (default: true)
* `/pulse config metricsModuleWriteQueue` Enables the write queue metrics module (event loop task savings tracking). (default: true)

---

## Permission Nodes
PulseNet uses the [Fabric Permissions API](https://github.com/lucko/fabric-permissions-api) for command permissions. Each node has a fallback vanilla permission level for servers without a permissions mod.

### General
| Node | Default | Description |
|:-----|:--------|:------------|
| `pulsenet.pulse` | `ALL` | Access the `/pulse` base command (shows version) |
| `pulsenet.reload` | `GAMEMASTERS` | Reload the PulseNet config file |
| `pulsenet.netstats` | `GAMEMASTERS` | View network, CPU, and memory statistics |
| `pulsenet.netstats.bar` | `GAMEMASTERS` | Toggle the metrics boss bar overlay |
| `pulsenet.netstats.reset` | `GAMEMASTERS` | Reset all metrics counters to zero |
| `pulsenet.packetnames` | `GAMEMASTERS` | List observed packet class names and channel IDs |

### Config
Config commands are generated dynamically per config value. The `<name>` below corresponds to the camelCase config key (e.g. `batchingEnabled`, `metricsUpdateInterval`).

| Node | Default | Description |
|:-----|:--------|:------------|
| `pulsenet.config` | `GAMEMASTERS` | List all config values |
| `pulsenet.config.<name>.get` | `GAMEMASTERS` | Read a specific config value |
| `pulsenet.config.<name>.set` | `GAMEMASTERS` | Change a specific config value |

---

## Support and Contributions

PulseNet is an open-source project by PulseMC. We welcome contributions regarding Netty, Mixin, and protocol-level optimizations.

* Discord: [Join our community](https://dsc.gg/Pulse-MC)
* Issues: [Report bugs on GitHub](https://github.com/Pulse-MC/PulseNet/issues)

---

## Support the Project

PulseNet is an open-source project maintained by the community. Hosting costs (Maven repo, build servers, website) are covered by donations. If PulseNet has helped your server's performance, consider supporting us on Boosty:

[**Support PulseMC on Boosty**](https://boosty.to/pulsedevs)

Your support keeps the project active and allows us to focus on building new, low-level networking optimizations.

---

<div align="center">
  <b>PulseNet: The Heartbeat of High-Performance Networking.</b>
</div>