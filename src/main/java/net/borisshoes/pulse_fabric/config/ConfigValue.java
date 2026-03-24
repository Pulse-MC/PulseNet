package net.borisshoes.pulse_fabric.config;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.borisshoes.pulse_fabric.utils.TextUtils;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class ConfigValue<T>{
   protected final T defaultValue;
   protected final String name;
   protected T value;
   protected Consumer<T> onSet;
   protected Validator<T> validator;
   
   public ConfigValue(@NotNull String name, T defaultValue, Consumer<T> onSet, Validator<T> validator){
      this.name = name;
      this.defaultValue = defaultValue;
      this.onSet = onSet;
      this.validator = validator;
   }
   
   public ConfigValue(@NotNull String name, T defaultValue){
      this(name, defaultValue, null, null);
   }
   
   public String getName(){
      return name;
   }
   
   public abstract T getFromString(String value);
   
   public abstract ArgumentType<?> getArgumentType();
   
   public abstract T parseArgumentValue(CommandContext<CommandSourceStack> ctx);
   
   public abstract CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder);
   
   public abstract String getValueString();
   
   public void setValue(T value){
      this.value = value;
      if(this.onSet != null){
         this.onSet.accept(value);
      }
   }
   
   public boolean validate(T value, @Nullable CommandContext<CommandSourceStack> ctx){
      if(this.validator != null){
         boolean pass = this.validator.predicate().test(value);
         if(!pass){
            this.validator.onFail().accept(value, ctx);
         }
         return pass;
      }
      return true;
   }
   
   public ConfigValue<T> setValidator(Validator<T> validator){
      this.validator = validator;
      return this;
   }
   
   public ConfigValue<T> setOnSet(Consumer<T> onSet){
      this.onSet = onSet;
      return this;
   }
   
   public String getComment(String modId){
      return getTranslation(this.name,modId,"comment");
   }
   
   public static String getTranslation(String name, String modId, String suffix){
      return "command."+modId+"."+ TextUtils.camelToSnake(name)+"."+suffix;
   }
   
   public static String getErrorTranslation(String modId){
      return "command."+modId+".error";
   }
}
