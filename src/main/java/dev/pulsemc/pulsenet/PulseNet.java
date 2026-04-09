package dev.pulsemc.pulsenet;

import com.mojang.serialization.Lifecycle;
import dev.pulsemc.pulsenet.config.ConfigManager;
import dev.pulsemc.pulsenet.config.ConfigSetting;
import dev.pulsemc.pulsenet.config.IConfigSetting;
import dev.pulsemc.pulsenet.config.values.*;
import dev.pulsemc.pulsenet.network.BatchingMode;
import dev.pulsemc.pulsenet.network.Metrics;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class PulseNet implements ModInitializer, ClientModInitializer {
   
   public static final Logger LOGGER = LogManager.getLogger("pulsenet");
   public static final String MOD_ID = "pulsenet";
   private static final String CONFIG_NAME = "pulse.properties";
   public static final Registry<IConfigSetting<?>> CONFIG_SETTINGS = new MappedRegistry<>(ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MOD_ID, "config_settings")), Lifecycle.stable());
   
   public static ConfigManager CONFIG;
   
   public static final IConfigSetting<?> BATCHING_ENABLED = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("batchingEnabled", true)));
   public static final IConfigSetting<?> BATCHING_MODE = registerConfigSetting(new ConfigSetting<>(
         new EnumConfigValue<>("batchingMode", BatchingMode.SMART_EXECUTION, BatchingMode.class)));
   public static final IConfigSetting<?> BATCHING_MAX_BATCH_SIZE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("batchingMaxBatchSize", 1024, new IntConfigValue.IntLimits(1, 4096))));
   public static final IConfigSetting<?> BATCHING_MAX_BATCH_BYTES = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("batchingMaxBatchBytes", 32000, new IntConfigValue.IntLimits(512, 64000))));
   public static final IConfigSetting<?> BATCHING_FLUSH_INTERVAL = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("batchingFlushInterval", 25, new IntConfigValue.IntLimits(1))));
   public static final IConfigSetting<?> BATCHING_SAFETY_MARGIN = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("batchingSafetyMarginBytes", 64)));
   public static final IConfigSetting<?> BATCHING_INSTANT_PACKETS = registerConfigSetting(new ConfigSetting<>(
         new ListConfigValue<>("batchingInstantPackets",
               List.of("ClientboundHurtAnimationPacket", "ClientboundDamageEventPacket", "ClientboundBlockEntityDataPacket"),
               new StringConfigValue("", ""))));
   public static final IConfigSetting<?> BATCHING_IGNORED_PACKETS = registerConfigSetting(new ConfigSetting<>(
         new ListConfigValue<>("batchingIgnoredPackets", List.of(), new StringConfigValue("", ""))));
   public static final IConfigSetting<?> BATCHING_INSTANT_CHANNELS = registerConfigSetting(new ConfigSetting<>(
         new ListConfigValue<>("batchingInstantChannels", List.of(), new StringConfigValue("", ""))));
   public static final IConfigSetting<?> BATCHING_IGNORED_CHANNELS = registerConfigSetting(new ConfigSetting<>(
         new ListConfigValue<>("batchingIgnoredChannels", List.of(), new StringConfigValue("", ""))));
   public static final IConfigSetting<?> BATCHING_CHAT_BYPASS = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("batchingChatPacketsBypass", true)));
   public static final IConfigSetting<?> BATCHING_OFF_THREAD_BYPASS = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("batchingOffThreadBypass", true)));
   public static final IConfigSetting<?> BATCHING_WRITE_QUEUE = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("batchingWriteQueue", true)));
   public static final IConfigSetting<?> BATCHING_PACKET_COALESCING = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("batchingPacketCoalescing", true)));
   public static final IConfigSetting<?> BATCHING_COALESCE_PACKETS = registerConfigSetting(new ConfigSetting<>(
         new ListConfigValue<>("batchingCoalescePackets",
               List.of("ClientboundLevelParticlesPacket", "ClientboundSoundPacket", "ClientboundSoundEntityPacket"),
               new StringConfigValue("", ""))));
   public static final IConfigSetting<?> BATCHING_COALESCE_BUNDLE_LIMIT = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("batchingCoalesceBundleLimit", 4000, new IntConfigValue.IntLimits(1, 4096))));
   public static final IConfigSetting<?> OPT_EXPLOSIONS_ENABLED = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("optExplosionsEnabled", true)));
   public static final IConfigSetting<?> OPT_EXPLOSIONS_THRESHOLD = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("optExplosionsBlockChangeThreshold", 512, new IntConfigValue.IntLimits(1))));
   public static final IConfigSetting<?> OPT_EXPLOSIONS_LOG = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("optExplosionsLogOptimizations", false)));
   public static final IConfigSetting<?> METRICS_ENABLED = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("metricsEnabled", true)));
   public static final IConfigSetting<?> METRICS_UPDATE_INTERVAL = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("metricsUpdateInterval", 1, new IntConfigValue.IntLimits(1, 60))));
   public static final IConfigSetting<?> METRICS_MODULE_NETWORK = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("metricsModuleNetwork", true)));
   public static final IConfigSetting<?> METRICS_MODULE_CPU = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("metricsModuleCpu", true)));
   public static final IConfigSetting<?> METRICS_MODULE_MEMORY = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("metricsModuleMemory", true)));
   public static final IConfigSetting<?> METRICS_MODULE_WRITE_QUEUE = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("metricsModuleWriteQueue", true)));
   
   @Override
   public void onInitialize(){
      CONFIG = new ConfigManager(MOD_ID, "pulsenet", CONFIG_NAME, CONFIG_SETTINGS);
      
      Metrics.start();
      CommandRegistrationCallback.EVENT.register(PulseNetCommands::register);
      
      LOGGER.info("Giving your server networking stack a Pulse!");
   }
   
   @Override
   public void onInitializeClient(){
      LOGGER.info("Giving your client networking stack a Pulse!");
   }
   
   private static IConfigSetting<?> registerConfigSetting(IConfigSetting<?> setting){
      Identifier identifier = Identifier.fromNamespaceAndPath(MOD_ID, setting.getId());
      Registry.register(CONFIG_SETTINGS, identifier, setting);
      return setting;
   }
}
