package dev.pulsemc.pulsenet.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.netty.channel.ChannelFutureListener;
import dev.pulsemc.pulsenet.PulseNet;
import dev.pulsemc.pulsenet.network.FlushReason;
import dev.pulsemc.pulsenet.network.PacketBuffer;
import dev.pulsemc.pulsenet.network.PacketBufferAccess;
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
   private PacketBuffer pulsenet$buffer;
   
   @Inject(method = "<init>", at = @At("TAIL"))
   private void pulsenet$initBuffer(CallbackInfo ci){
      this.pulsenet$buffer = new PacketBuffer((ServerCommonPacketListenerImpl) (Object) this, this.connection, this.server);
   }
   
   @Override
   public PacketBuffer pulsenet$getBuffer(){
      return this.pulsenet$buffer;
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
   private void pulsenet$wrapConnectionSend(Connection connection, Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush, Operation<Void> original){
      // When batching is enabled, delegate to the PacketBuffer for flush control
      if(pulsenet$buffer != null && PulseNet.CONFIG != null && PulseNet.CONFIG.getBoolean(PulseNet.BATCHING_ENABLED)){
         pulsenet$buffer.handleOutgoingPacket(packet, listener, flush, original, connection);
         
         // Handle terminal packets
         if(packet.isTerminal()){
            pulsenet$buffer.flush(FlushReason.INSTANT);
         }
         return;
      }
      
      // Batching disabled — vanilla behavior
      original.call(connection, packet, listener, flush);
   }
   
   @Inject(method = "onDisconnect", at = @At("HEAD"))
   private void pulsenet$cleanupOnDisconnect(CallbackInfo ci){
      if(pulsenet$buffer != null){
         pulsenet$buffer.cleanup();
      }
   }
}
