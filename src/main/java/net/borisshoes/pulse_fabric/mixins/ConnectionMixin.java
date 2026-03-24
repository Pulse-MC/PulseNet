package net.borisshoes.pulse_fabric.mixins;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import net.borisshoes.pulse_fabric.network.ConnectionAccess;
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
   public abstract void pulse_fabric$invokeSend(Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush);
   
   @Override
   public Channel pulse_fabric$getChannel(){
      return this.channel;
   }
   
   @Override
   public void pulse_fabric$flushChannel(){
      if(this.channel != null && this.channel.isOpen()){
         this.channel.flush();
      }
   }
   
   @Override
   public void pulse_fabric$sendNoFlush(Packet<?> packet, @Nullable ChannelFutureListener listener){
      pulse_fabric$invokeSend(packet, listener, false);
   }
}
