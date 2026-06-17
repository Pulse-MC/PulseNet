package dev.pulsemc.pulsenet.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;

import java.util.Locale;

public class TextUtils {
   
   public static String camelToSnake(String str){
      return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase(Locale.ROOT);
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
