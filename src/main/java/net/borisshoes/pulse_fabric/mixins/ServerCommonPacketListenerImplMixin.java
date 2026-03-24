package net.borisshoes.pulse_fabric.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.netty.channel.ChannelFutureListener;
import net.borisshoes.pulse_fabric.PulseFabric;
import net.borisshoes.pulse_fabric.network.FlushReason;
import net.borisshoes.pulse_fabric.network.PacketBuffer;
import net.borisshoes.pulse_fabric.network.PacketBufferAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin implements PacketBufferAccess {
   
   @Shadow
   @Final
   protected MinecraftServer server;
   
   @Shadow
   @Final
   protected Connection connection;
   
   @Unique
   private PacketBuffer pulse_fabric$buffer;
   
   @Inject(method = "<init>", at = @At("TAIL"))
   private void pulse_fabric$initBuffer(CallbackInfo ci){
      this.pulse_fabric$buffer = new PacketBuffer((ServerCommonPacketListenerImpl) (Object) this, this.connection, this.server);
   }
   
   @Override
   public PacketBuffer pulse_fabric$getBuffer(){
      return this.pulse_fabric$buffer;
   }
   
   /**
    * Wraps the {@code connection.send(packet, listener, flush)} call INSIDE
    * {@code ServerCommonPacketListenerImpl.send(Packet, ChannelFutureListener)}.
    * <p>
    * This fires AFTER all other mixins on {@code send()} have run (Polymer's packet
    * transformers, server-side translations, virtual entity handlers, etc.).
    * By the time we see the packet here, it is fully transformed.
    * We only control whether the underlying write flushes immediately or is deferred.
    */
   @WrapOperation(
         method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V",
         at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V"))
   private void pulse_fabric$wrapConnectionSend(Connection connection, Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush, Operation<Void> original){
      // When batching is enabled, delegate to the PacketBuffer for flush control
      if(pulse_fabric$buffer != null && PulseFabric.CONFIG != null && PulseFabric.CONFIG.getBoolean(PulseFabric.BATCHING_ENABLED)){
         pulse_fabric$buffer.handleOutgoingPacket(packet, listener, flush, original, connection);
         
         // Handle terminal packets
         if(packet.isTerminal()){
            pulse_fabric$buffer.flush(FlushReason.INSTANT);
         }
         return;
      }
      
      // Batching disabled — vanilla behavior
      original.call(connection, packet, listener, flush);
   }
   
   @Inject(method = "onDisconnect", at = @At("HEAD"))
   private void pulse_fabric$cleanupOnDisconnect(CallbackInfo ci){
      if(pulse_fabric$buffer != null){
         pulse_fabric$buffer.cleanup();
      }
   }
}
