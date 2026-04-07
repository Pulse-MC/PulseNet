package dev.pulsemc.pulsenet.config;

public interface IConfigSetting<T>{
   ConfigValue<T> makeConfigValue();
   String getId();
   String getName();
}