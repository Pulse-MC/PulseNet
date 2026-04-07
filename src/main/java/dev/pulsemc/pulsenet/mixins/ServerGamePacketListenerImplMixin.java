package dev.pulsemc.pulsenet.mixins;

import dev.pulsemc.pulsenet.PulseNet;
import dev.pulsemc.pulsenet.network.FlushReason;
import dev.pulsemc.pulsenet.network.MetricsBar;
import dev.pulsemc.pulsenet.network.PacketBuffer;
import dev.pulsemc.pulsenet.network.PacketBufferAccess;
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
   private void pulsenet$flushOnTickEnd(CallbackInfo ci){
      if(PulseNet.CONFIG == null || !PulseNet.CONFIG.getBoolean(PulseNet.BATCHING_ENABLED)) return;
      PacketBuffer buffer = ((PacketBufferAccess) this).pulsenet$getBuffer();
      if(buffer != null){
         buffer.flush(FlushReason.TICK);
      }
   }
   
   @Inject(method = "onDisconnect", at = @At("HEAD"))
   private void pulsenet$cleanupOnDisconnect(CallbackInfo ci){
      PacketBuffer buffer = ((PacketBufferAccess) this).pulsenet$getBuffer();
      if(buffer != null){
         buffer.cleanup();
      }
      if(player != null){
         MetricsBar.removePlayer(player);
      }
   }
}
