package dev.pulsemc.pulsenet.mixins;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import dev.pulsemc.pulsenet.network.ConnectionAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Connection.class)
public abstract class ConnectionMixin implements ConnectionAccess {
   
   @Shadow
   private Channel channel;
   
   @Invoker("send")
   public abstract void pulsenet$invokeSend(Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush);
   
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
