package net.borisshoes.pulse_fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.borisshoes.pulse_fabric.network.Metrics;
import net.borisshoes.pulse_fabric.network.MetricsBar;
import net.borisshoes.pulse_fabric.network.PacketBuffer;
import net.borisshoes.pulse_fabric.utils.TextUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.FloatArgumentType.getFloat;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.EntityArgument.entity;
import static net.minecraft.commands.arguments.EntityArgument.getEntity;
import static net.minecraft.commands.arguments.IdentifierArgument.getId;
import static net.minecraft.commands.arguments.IdentifierArgument.id;
import static net.minecraft.commands.arguments.item.ItemArgument.getItem;
import static net.minecraft.commands.arguments.item.ItemArgument.item;

public class FabricPulseCommands {
   
   public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext access, Commands.CommandSelection env){
      dispatcher.register(literal("pulse").executes(FabricPulseCommands::getVersion)
            .then(literal("reload").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                  .executes(FabricPulseCommands::reloadConfig))
            .then(literal("netstats").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                  .executes(ctx -> sendStats(ctx, "all"))
                  .then(literal("bar").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                        .executes(FabricPulseCommands::toggleBar))
                  .then(literal("reset").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                        .executes(FabricPulseCommands::resetStats))
                  .then(argument("type", word())
                        .suggests((ctx, builder) -> {
                           builder.suggest("network");
                           builder.suggest("cpu");
                           builder.suggest("ram");
                           builder.suggest("all");
                           return builder.buildFuture();
                        })
                        .executes(ctx -> sendStats(ctx, getString(ctx, "type")))))
      );
      dispatcher.register(PulseFabric.CONFIG.generateCommand("pulse","config"));
   }
   
   private static int getVersion(CommandContext<CommandSourceStack> context){
      context.getSource().sendSuccess(() -> Component.literal("PulseMC-Fabric " + FabricLoader.getInstance().getModContainer(PulseFabric.MOD_ID).get().getMetadata().getVersion().getFriendlyString()), false);
      return 1;
   }
   
   // ─── Network Optimization Commands ──────────────────────────────────
   
   private static int reloadConfig(CommandContext<CommandSourceStack> context){
      if(PulseFabric.CONFIG != null){
         PulseFabric.CONFIG.read();
         PulseFabric.CONFIG.save();
      }
      PacketBuffer.reload();
      Metrics.reload();
      MetricsBar.reload();
      context.getSource().sendSuccess(() -> Component.translatable("command.pulse-fabric.reload.success").withStyle(ChatFormatting.GREEN), true);
      return 1;
   }
   
   private static int toggleBar(CommandContext<CommandSourceStack> context){
      ServerPlayer player = context.getSource().getPlayer();
      if(player == null){
         context.getSource().sendFailure(Component.translatable("text.pulse-fabric.must_be_executed_by_player"));
         return 0;
      }
      boolean showing = MetricsBar.toggle(player);
      if(showing){
         context.getSource().sendSuccess(() -> Component.translatable("command.pulse-fabric.netstats.bar.enabled").withStyle(ChatFormatting.GREEN), false);
      }else{
         context.getSource().sendSuccess(() -> Component.translatable("command.pulse-fabric.netstats.bar.disabled").withStyle(ChatFormatting.YELLOW), false);
      }
      return 1;
   }
   
   private static int sendStats(CommandContext<CommandSourceStack> context, String type){
      switch(type.toLowerCase(Locale.ROOT)){
         case "network" -> sendNetworkStats(context);
         case "cpu" -> sendCpuStats(context);
         case "ram" -> sendRamStats(context);
         case "all" -> {
            sendNetworkStats(context);
            sendCpuStats(context);
            sendRamStats(context);
         }
         default -> context.getSource().sendFailure(Component.translatable("command.pulse-fabric.netstats.unknown_type", type));
      }
      return 1;
   }
   
   private static void sendNetworkStats(CommandContext<CommandSourceStack> context){
      long logical = (long) Metrics.ppsLogical;
      long physical = (long) Metrics.ppsPhysical;
      long saved = logical - physical;
      String savePct = String.format("%.1f", logical > 0 ? (saved * 100.0 / logical) : 0);
      
      double speed = Metrics.networkSpeedKbs;
      String speedStr = speed > 1024 ? String.format("%.2f MB/s", speed / 1024.0) : String.format("%.1f KB/s", speed);
      
      context.getSource().sendSuccess(() -> Component.translatable("command.pulse-fabric.netstats.network.header").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
      context.getSource().sendSuccess(() -> Component.translatable("command.pulse-fabric.netstats.network.pps_logical", logical).withStyle(ChatFormatting.WHITE), false);
      context.getSource().sendSuccess(() -> Component.translatable("command.pulse-fabric.netstats.network.pps_physical", physical).withStyle(ChatFormatting.AQUA), false);
      context.getSource().sendSuccess(() -> Component.translatable("command.pulse-fabric.netstats.network.calls_saved", saved, savePct).withStyle(ChatFormatting.GREEN), false);
      context.getSource().sendSuccess(() -> Component.empty(), false);
      context.getSource().sendSuccess(() -> Component.translatable("command.pulse-fabric.netstats.network.bandwidth", speedStr).withStyle(ChatFormatting.WHITE), false);
      context.getSource().sendSuccess(() -> Component.translatable("command.pulse-fabric.netstats.network.optimized_chunks", Metrics.optimizedChunks.get()).withStyle(ChatFormatting.YELLOW), false);
      context.getSource().sendSuccess(() -> Component.empty(), false);
      long wqQueued = (long) Metrics.writeQueuedPps;
      long wqTasks = (long) Metrics.eventLoopTasksPps;
      long wqSaved = (long) Metrics.savedTasksPps;
      long wqTotalSaved = Metrics.totalSavedTasks.get();
      String wqTotalStr = TextUtils.readableLong(wqTotalSaved);
      context.getSource().sendSuccess(() -> Component.translatable("command.pulse-fabric.netstats.network.write_queue_header").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
      context.getSource().sendSuccess(() -> Component.translatable("command.pulse-fabric.netstats.network.write_queue_rate", wqQueued, wqTasks).withStyle(ChatFormatting.WHITE), false);
      context.getSource().sendSuccess(() -> Component.translatable("command.pulse-fabric.netstats.network.write_queue_saved", wqSaved, wqTotalStr).withStyle(ChatFormatting.GREEN), false);
   }
   
   private static void sendCpuStats(CommandContext<CommandSourceStack> context){
      String current = String.format("%.2f", Metrics.cpuUsage);
      String vanilla = String.format("%.2f", Metrics.vanillaCpuEst);
      String delta = String.format("%.3f", Metrics.vanillaCpuEst - Metrics.cpuUsage);
      context.getSource().sendSuccess(() -> Component.translatable("command.pulse-fabric.netstats.cpu.header").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
      context.getSource().sendSuccess(() -> Component.translatable("command.pulse-fabric.netstats.cpu.current", current).withStyle(ChatFormatting.WHITE), false);
      context.getSource().sendSuccess(() -> Component.translatable("command.pulse-fabric.netstats.cpu.vanilla_est", vanilla).withStyle(ChatFormatting.WHITE), false);
      context.getSource().sendSuccess(() -> Component.empty(), false);
      context.getSource().sendSuccess(() -> Component.translatable("command.pulse-fabric.netstats.cpu.efficiency", delta).withStyle(ChatFormatting.GREEN), false);
   }
   
   private static void sendRamStats(CommandContext<CommandSourceStack> context){
      long savedBytes = Metrics.savedAllocationsBytes.get();
      String savedStr;
      if(savedBytes > 1024 * 1024 * 1024){
         savedStr = String.format("%.2f GB", savedBytes / (1024.0 * 1024.0 * 1024.0));
      }else if(savedBytes > 1024 * 1024){
         savedStr = String.format("%.0f MB", savedBytes / (1024.0 * 1024.0));
      }else if(savedBytes > 1024){
         savedStr = String.format("%.0f KB", savedBytes / 1024.0);
      }else{
         savedStr = savedBytes + " B";
      }
      
      context.getSource().sendSuccess(() -> Component.translatable("command.pulse-fabric.netstats.ram.header").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
      context.getSource().sendSuccess(() -> Component.translatable("command.pulse-fabric.netstats.ram.saved", savedStr).withStyle(ChatFormatting.WHITE), false);
   }
   
   private static int resetStats(CommandContext<CommandSourceStack> context){
      Metrics.reset();
      context.getSource().sendSuccess(() -> Component.translatable("command.pulse-fabric.netstats.reset.success").withStyle(ChatFormatting.GREEN), true);
      return 1;
   }
}
