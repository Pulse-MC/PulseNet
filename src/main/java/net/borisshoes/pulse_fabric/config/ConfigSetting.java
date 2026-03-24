package net.borisshoes.pulse_fabric.config;

import net.borisshoes.pulse_fabric.utils.TextUtils;

import java.util.Objects;

public record ConfigSetting<T>(ConfigValue<T> setting) implements IConfigSetting<T>{
   public ConfigSetting(ConfigValue<T> setting){
      this.setting = Objects.requireNonNull(setting);
   }
   
   public ConfigValue<T> makeConfigValue(){
      return setting;
   }
   
   public String getId(){
      return TextUtils.camelToSnake(setting.getName());
   }
   
   public String getName(){
      return setting.getName();
   }
}