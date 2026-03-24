package net.borisshoes.pulse_fabric.config;

public interface IConfigSetting<T>{
   ConfigValue<T> makeConfigValue();
   String getId();
   String getName();
}