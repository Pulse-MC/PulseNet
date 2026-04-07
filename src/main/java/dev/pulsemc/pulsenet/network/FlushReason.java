package dev.pulsemc.pulsenet.network;

public enum FlushReason {
   LIMIT_BYTES,
   LIMIT_COUNT,
   TICK,
   INSTANT,
   INTERVAL,
   MANUAL
}

