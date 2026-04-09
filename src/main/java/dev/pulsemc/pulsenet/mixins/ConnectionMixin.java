package dev.pulsemc.pulsenet.mixins;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import dev.pulsemc.pulsenet.network.ConnectionAccess;
import dev.pulsemc.pulsenet.network.FlushReason;
import dev.pulsemc.pulsenet.network.PacketBuffer;
import dev.pulsemc.pulsenet.network.PacketBufferAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ConnectionMixin implements ConnectionAccess {
   
   @Shadow
   private Channel channel;
   
   @Shadow
   private volatile PacketListener packetListener;
   
   @Invoker("send")
   public abstract void pulsenet$invokeSend(Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush);
   
   /**
    * Flushes the PulseNet buffer before Fabric API's channel registration packets.
    * <p>
    * Fabric's networking addon sends {@code minecraft:register} / {@code minecraft:unregister}
    * via {@code connection.send()} directly (in {@code AbstractChanneledNetworkAddon.sendPacket()}),
    * completely bypassing {@code ServerCommonPacketListenerImpl.send()} and therefore PulseNet's
    * {@code @WrapOperation}. Without this hook, the registration packet is queued on the Netty
    * event loop AFTER PulseNet's buffered data, causing it to arrive at the client behind a
    * potentially large batch of game data. Mods with timing-sensitive join handshakes
    * (e.g. Axiom's one-shot {@code hasJoined} check) fail because the registration arrives
    * too late — after the client's first tick has already processed an empty channel list.
    * <p>
    * By flushing PulseNet's write queue here (before Netty even sees the registration packet),
    * we ensure all buffered game data is submitted to the event loop first, then the registration
    * packet is submitted immediately after. The client receives them in the correct order with
    * no artificial batching delay on the registration packet.
    * <p>
    * The {@code !flush} guard prevents this from firing on PulseNet's own internal
    * {@code pulsenet$sendNoFlush()} calls (which use {@code flush=false}).
    */
   @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("HEAD"))
   private void pulsenet$flushBeforeChannelRegistration(Packet<?> packet, @Nullable ChannelFutureListener callback, boolean flush, CallbackInfo ci){
      if(!flush) return;
      
      if(packet instanceof ClientboundCustomPayloadPacket(CustomPacketPayload payload)){
         String channelId = payload.type().id().toString();
         if("minecraft:register".equals(channelId) || "minecraft:unregister".equals(channelId)){
            PacketListener pl = this.packetListener;
            if(pl instanceof PacketBufferAccess access){
               PacketBuffer buffer = access.pulsenet$getBuffer();
               if(buffer != null){
                  buffer.flush(FlushReason.INSTANT);
               }
            }
         }
      }
   }
   
   @Override
   public Channel pulsenet$getChannel(){
      return this.channel;
   }
   
   @Override
   public void pulsenet$flushChannel(){
      if(this.channel != null && this.channel.isOpen()){
         this.channel.flush();
      }
   }
   
   @Override
   public void pulsenet$sendNoFlush(Packet<?> packet, @Nullable ChannelFutureListener listener){
      pulsenet$invokeSend(packet, listener, false);
   }
}
