package dev.pulsemc.pulsenet.network;

/**
 * Duck interface applied to ServerCommonPacketListenerImpl via mixin
 * to attach a PacketBuffer instance to each connection.
 */
public interface PacketBufferAccess {
   PacketBuffer pulsenet$getBuffer();
}

