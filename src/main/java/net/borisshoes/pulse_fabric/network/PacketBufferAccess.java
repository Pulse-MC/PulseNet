package net.borisshoes.pulse_fabric.network;

/**
 * Duck interface applied to ServerCommonPacketListenerImpl via mixin
 * to attach a PacketBuffer instance to each connection.
 */
public interface PacketBufferAccess {
   PacketBuffer pulse_fabric$getBuffer();
}

