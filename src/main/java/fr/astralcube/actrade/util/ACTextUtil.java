package fr.astralcube.actrade.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class ACTextUtil {
  public static final DynamicCommandExceptionType INVALID_COMPONENT_EXCEPTION = new DynamicCommandExceptionType(text -> new TranslatableText("argument.component.invalid", text));
    
    public static Text parse(StringReader stringReader) throws CommandSyntaxException {
        try {
            MutableText text = Text.Serializer.fromJson(stringReader);
            if (text == null) {
                throw INVALID_COMPONENT_EXCEPTION.createWithContext(stringReader, "empty");
            }
            return text;
        }
        catch (Exception exception) {
            String string = exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage();
            throw INVALID_COMPONENT_EXCEPTION.createWithContext(stringReader, string);
        }
    }
}
