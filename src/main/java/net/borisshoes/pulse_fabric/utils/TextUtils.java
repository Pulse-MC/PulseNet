package net.borisshoes.pulse_fabric.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {
   
   public static String camelToSnake(String str){
      return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase(Locale.ROOT);
   }
   
   public static final ArrayList<Tuple<ChatFormatting,Integer>> COLOR_MAP = new ArrayList<>(Arrays.asList(
         new Tuple<>(ChatFormatting.BLACK,0x000000),
         new Tuple<>(ChatFormatting.DARK_BLUE,0x0000AA),
         new Tuple<>(ChatFormatting.DARK_GREEN,0x00AA00),
         new Tuple<>(ChatFormatting.DARK_AQUA,0x00AAAA),
         new Tuple<>(ChatFormatting.DARK_RED,0xAA0000),
         new Tuple<>(ChatFormatting.DARK_PURPLE,0xAA00AA),
         new Tuple<>(ChatFormatting.GOLD,0xFFAA00),
         new Tuple<>(ChatFormatting.GRAY,0xAAAAAA),
         new Tuple<>(ChatFormatting.DARK_GRAY,0x555555),
         new Tuple<>(ChatFormatting.BLUE,0x5555FF),
         new Tuple<>(ChatFormatting.GREEN,0x55FF55),
         new Tuple<>(ChatFormatting.AQUA,0x55FFFF),
         new Tuple<>(ChatFormatting.RED,0xFF5555),
         new Tuple<>(ChatFormatting.LIGHT_PURPLE,0xFF55FF),
         new Tuple<>(ChatFormatting.YELLOW,0xFFFF55),
         new Tuple<>(ChatFormatting.WHITE,0xFFFFFF)
   ));
   
   public static ChatFormatting getClosestFormatting(int colorRGB){
      ChatFormatting closest = ChatFormatting.WHITE;
      double cDist = Integer.MAX_VALUE;
      for(Tuple<ChatFormatting, Integer> pair : COLOR_MAP){
         int repColor = pair.getB();
         double rDist = (((repColor>>16)&0xFF)-((colorRGB>>16)&0xFF))*0.30;
         double gDist = (((repColor>>8)&0xFF)-((colorRGB>>8)&0xFF))*0.59;
         double bDist = ((repColor&0xFF)-(colorRGB&0xFF))*0.11;
         double dist = rDist*rDist + gDist*gDist + bDist*bDist;
         if(dist < cDist){
            cDist = dist;
            closest = pair.getA();
         }
      }
      return closest;
   }
   
   public static String intToRoman(int num){
      int[] values = {1000,900,500,400,100,90,50,40,10,9,5,4,1};
      String[] romanLetters = {"M","CM","D","CD","C","XC","L","XL","X","IX","V","IV","I"};
      StringBuilder roman = new StringBuilder();
      for(int i=0;i<values.length;i++)
      {
         while(num >= values[i])
         {
            num = num - values[i];
            roman.append(romanLetters[i]);
         }
      }
      return roman.toString();
   }
   
   public static String readableLong(long value){
      if(value >= 1_000_000_000) return String.format("%.2fB", value / 1_000_000_000.0);
      if(value >= 1_000_000) return String.format("%.1fM", value / 1_000_000.0);
      if(value >= 1_000) return String.format("%.1fK", value / 1_000.0);
      return String.valueOf(value);
   }
   
   public static String readableInt(int num){
      return String.format("%,d", num);
   }
   
   public static String readableDouble(double num){
      return readableDouble(num,2);
   }
   
   public static String readableDouble(double num, int decimalPlaces){
      return String.format("%,0"+(decimalPlaces+1)+"."+decimalPlaces+"f", num);
   }
   
   public static MutableComponent removeItalics(Component text){
      return removeItalics(Component.literal("").append(text));
   }
   
   public static MutableComponent removeItalics(MutableComponent text){
      Style parentStyle = Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withItalic(false).withBold(false).withUnderlined(false).withObfuscated(false).withStrikethrough(false);
      return text.setStyle(text.getStyle().applyTo(parentStyle));
   }
}
