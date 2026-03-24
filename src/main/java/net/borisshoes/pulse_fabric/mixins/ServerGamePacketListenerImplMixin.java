package net.borisshoes.pulse_fabric.mixins;

import net.borisshoes.pulse_fabric.PulseFabric;
import net.borisshoes.pulse_fabric.network.FlushReason;
import net.borisshoes.pulse_fabric.network.MetricsBar;
import net.borisshoes.pulse_fabric.network.PacketBuffer;
import net.borisshoes.pulse_fabric.network.PacketBufferAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
   
   @Shadow
   public ServerPlayer player;
   
   @Inject(method = "tick", at = @At("TAIL"))
   private void pulse_fabric$flushOnTickEnd(CallbackInfo ci){
      if(PulseFabric.CONFIG == null || !PulseFabric.CONFIG.getBoolean(PulseFabric.BATCHING_ENABLED)) return;
      PacketBuffer buffer = ((PacketBufferAccess) this).pulse_fabric$getBuffer();
      if(buffer != null){
         buffer.flush(FlushReason.TICK);
      }
   }
   
   @Inject(method = "onDisconnect", at = @At("HEAD"))
   private void pulse_fabric$cleanupOnDisconnect(CallbackInfo ci){
      PacketBuffer buffer = ((PacketBufferAccess) this).pulse_fabric$getBuffer();
      if(buffer != null){
         buffer.cleanup();
      }
      if(player != null){
         MetricsBar.removePlayer(player);
      }
   }
}
