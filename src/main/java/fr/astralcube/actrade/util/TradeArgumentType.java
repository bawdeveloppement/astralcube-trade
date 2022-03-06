package fr.astralcube.actrade.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import fr.astralcube.actrade.ACTrade;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

public class TradeArgumentType implements ArgumentType<UUID> {

    public static final SimpleCommandExceptionType INVALID_UUID = new SimpleCommandExceptionType(new TranslatableText("argument.uuid.invalid"));
    private static final Collection<String> EXAMPLES = Arrays.asList("dd12be42-52a9-4a91-a8a1-11c01849e498");
    private static final Pattern VALID_CHARACTERS = Pattern.compile("^([-A-Fa-f0-9]+)");

    public static UUID getUuid(CommandContext<ServerCommandSource> context, String name) {
        return context.getArgument(name, UUID.class);
    }

    public static TradeArgumentType uuid() {
        return new TradeArgumentType();
    }

    @Override
    public UUID parse(StringReader stringReader) throws CommandSyntaxException {
        String string = stringReader.getRemaining();
        Matcher matcher = VALID_CHARACTERS.matcher(string);
        if (matcher.find()) {
            String string2 = matcher.group(1);
            try {
                UUID uUID = UUID.fromString(string2);
                stringReader.setCursor(stringReader.getCursor() + string2.length());
                return uUID;
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
        throw INVALID_UUID.create();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder2) {
        if (context.getSource() instanceof CommandSource) {
            StringReader stringReader = new StringReader(builder2.getInput());
            stringReader.setCursor(builder2.getStart());
            Collection<String> collection = ACTradeUtil.getTradesUUIDOfPlayer(MinecraftServer.ANONYMOUS_PLAYER_PROFILE.getName(), ACTrade.mapTrades);
            return CommandSource.suggestMatching(collection, builder2);
        }
        return Suggestions.empty();
    }
}
