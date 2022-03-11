package fr.astralcube.actrade.commands;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import javax.sound.midi.Receiver;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import fr.astralcube.actrade.ACTrade;
import fr.astralcube.actrade.Trade;
import fr.astralcube.actrade.Trade.TradeState;
import fr.astralcube.actrade.handler.ACScreenProvider;
import fr.astralcube.actrade.util.ACTextUtil;
import fr.astralcube.actrade.util.ACTradeUtil;
import fr.astralcube.actrade.util.TradeArgumentType;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;


public class ACTradeCommand {
    
    public static final DynamicCommandExceptionType INVALID_COMPONENT_EXCEPTION = new DynamicCommandExceptionType(text -> new TranslatableText("argument.component.invalid", text));

    private static void registerRequest(CommandNode<ServerCommandSource> root) {
		  LiteralCommandNode<ServerCommandSource> trade = CommandManager.literal("request").build();
        root.addChild(trade);
		  ArgumentCommandNode<ServerCommandSource, EntitySelector> playerArg = CommandManager.argument("player", EntityArgumentType.player()).executes(ctx -> {
        ServerPlayerEntity receiverPlayer = EntityArgumentType.getPlayer(ctx, "player");
        ServerPlayerEntity senderPlayer = ctx.getSource().getPlayer();

        UUID senderPlayerUuid = senderPlayer.getUuid();
        boolean tradeExist = false;
        // If the receiverPlayer is not us
        // if (receiverPlayer.getUuid() != senderPlayerUuid) {
          for (Map.Entry<UUID, Trade> t : ACTrade.mapTrades.entrySet()) {
            UUID tempUuid = t.getKey();
            Trade tempTrade = t.getValue();
            // If a trade with the receiver and sender already exist
            if (tempTrade.senderUuid == senderPlayerUuid && tempTrade.receiverUuid == receiverPlayer.getUuid()) {
              // A TRADE EXIST AND HAS FINISHED
              if (tempTrade.currentTradeState == TradeState.FINISH) {
                ACTrade.mapTrades.get(t.getKey()).reset();
                ctx.getSource().sendFeedback(new TranslatableText("actrade.command.request_already_sent"), false);
                receiverPlayer.sendMessage(new TranslatableText(
                  "actrade.command.request",
                  ACTextUtil.parse(new StringReader("{\"text\":\""+ ctx.getSource().getPlayer().getEntityName() +"\", \"color\":\"yellow\"}")), 
                  ACTextUtil.parse(new StringReader("{\"translate\":\"actrade.command.accept_trade_text_button\",\"color\":\"blue\",\"clickEvent\":{\"action\":\"run_command\",\"\":\"blue\",\"value\":\"/trade accept " + tempUuid.toString() +"\"}}"))), false);
              }
              // A TRADE EXIST AND HAS ALREADY STARTED
              else if (tempTrade.currentTradeState == TradeState.STARTED) {
                ctx.getSource().sendFeedback(new TranslatableText(
                  "actrade.command.trade_with_x_already_started_open_inv",
                  getColoredText(senderPlayer.getEntityName(), "yellow"),
                  getTextRawButtonColor("Open", "/trade open " + tempUuid, "green"),
                  getTextRawButtonColor("Close", "/trade close " + tempUuid, "red")
                ), false);
              }
              // THE REQUEST IS ALREADY PENDING
              else {
                ctx.getSource().sendFeedback(new TranslatableText("actrade.command.the_request_is_already_pending"), false);
              }
              tradeExist = true;
            }
          }
          
          if (!tradeExist) {
            UUID newUUID = UUID.randomUUID();
            Trade newTrade = new Trade(newUUID, senderPlayerUuid, receiverPlayer.getUuid());
            ACTrade.mapTrades.put(newUUID, newTrade);
            receiverPlayer.sendMessage(new TranslatableText("actrade.command.request", ACTextUtil.parse(new StringReader("{\"text\":\""+ ctx.getSource().getPlayer().getEntityName() +"\", \"color\":\"yellow\"}")), ACTextUtil.parse(new StringReader("{\"translate\":\"actrade.command.accept_trade_text_button\",\"color\":\"blue\",\"clickEvent\":{\"action\":\"run_command\",\"\":\"blue\",\"value\":\"/trade accept " + newUUID.toString() +"\"}}"))), false);
          }

          return 1;

      }).build();
      trade.addChild(playerArg);
    }

    private static void registerAccept (CommandNode<ServerCommandSource> root) {
		  LiteralCommandNode<ServerCommandSource> trade = CommandManager.literal("accept").build();
      root.addChild(trade);

      ArgumentCommandNode<ServerCommandSource, UUID> tradIdArg = CommandManager.argument("tradeId", TradeArgumentType.trades(TradeState.PENDING)).executes(ctx -> {
        ServerPlayerEntity thisPlayer = ctx.getSource().getPlayer();
        UUID targetTradeUuid = TradeArgumentType.getTrade(ctx, "tradeId", thisPlayer.getUuid());
        // Trade targetTrade = ACTrade.mapTrades.get(targetTradeUuid);

        for (Map.Entry<UUID, Trade> t : ACTrade.mapTrades.entrySet()) {
          UUID currentTradeUuid = t.getKey();
          Trade currentTrade = t.getValue();
          // STADE 1: limit one trade
          // STADE 2: TOD: accept multiple trade but expire after x second if not validated;
          // If the senderPlayer is the same as our and the trades is already started -> PLAYER_ALREADY_TRADING

          if (currentTradeUuid.compareTo(targetTradeUuid) == 0) {
            System.out.println("[SENDER] current trade uuid");

            if ( ACTradeUtil.areWeSender(currentTradeUuid, thisPlayer.getUuid())) {
              System.out.println("[SENDER] We are sender");
              ServerPlayerEntity oppositeReceiver = (ServerPlayerEntity)ctx.getSource().getWorld().getEntity(currentTrade.receiverUuid);

              if ( ACTradeUtil.isAlreadyInOtherTradeThan(currentTrade.receiverUuid, currentTradeUuid)) {
                System.out.print("[RECEIVER] %S is in other trade");
                System.out.println(oppositeReceiver.getEntityName());
                // Opposite receiver already in trade
                thisPlayer.sendMessage(
                  new TranslatableText(
                    "actrade.command.opposite_x_already_trading_with_another_player", 
                    ACTextUtil.parse(new StringReader("{\"text\":\""+ oppositeReceiver.getEntityName() +"\", \"color\":\"yellow\"}"))
                  ),
                  true
                );
              } else {
                if (currentTrade.currentTradeState == Trade.TradeState.PENDING) {
                  System.out.println("[SENDER] trade is pending");
                  currentTrade.currentTradeState = TradeState.STARTED;
                  thisPlayer.sendMessage(
                    new TranslatableText(
                      "actrade.command.trade_accepted_you_can_open_inv",
                      ACTextUtil.parse(new StringReader("{\"text\":\""+ oppositeReceiver.getEntityName() +"\", \"color\":\"yellow\"}")),
                      ACTradeCommand.getTextRawButton("Open TradeBox", "/trade open " + currentTradeUuid.toString())
                      
                    ),
                    true
                  );
                  oppositeReceiver.sendMessage(
                    new TranslatableText(
                      "actrade.command.trade_accepted_you_can_open_inv",
                      ACTextUtil.parse(new StringReader("{\"text\":\""+ thisPlayer.getEntityName() +"\", \"color\":\"yellow\"}")),
                      ACTradeCommand.getTextRawButton("Open TradeBox", "/trade open " + currentTradeUuid.toString())
                    ),
                    false
                  );
                } else if (currentTrade.currentTradeState == Trade.TradeState.STARTED) {
                  System.out.println("[SENDER] trade is already started");

                  thisPlayer.sendMessage(new TranslatableText("actrade.command.trade_already_started"), false);
                } else {
                  System.out.println("[SENDER] trade is already finished");
                  thisPlayer.sendMessage(new TranslatableText("actrade.command.trade_already_finished"), false);
                  ACTrade.mapTrades.remove(currentTradeUuid);
                }
              }
            } else {
              System.out.println("[SENDER] We are receiver");
              ServerPlayerEntity oppositeSender = (ServerPlayerEntity)ctx.getSource().getWorld().getEntity(currentTrade.senderUuid);
              
              if ( ACTradeUtil.isAlreadyInOtherTradeThan(currentTrade.senderUuid, currentTradeUuid)) {
                System.out.print("[SENDER] %s is in other trade");
                System.out.println(oppositeSender.getEntityName());
                // Opposite sender already in trade
                thisPlayer.sendMessage(
                  new TranslatableText(
                    "actrade.command.opposite_x_already_trading_with_another_player", 
                    ACTextUtil.parse(new StringReader("{\"text\":\""+ oppositeSender.getEntityName() +"\", \"color\":\"yellow\"}"))
                  ),
                  true
                );
              } else {
                if (currentTrade.currentTradeState == Trade.TradeState.PENDING) {
                System.out.print("[SENDER] %s pending");
                System.out.println(oppositeSender.getEntityName());
                currentTrade.currentTradeState = TradeState.STARTED;

                  thisPlayer.sendMessage(
                    new TranslatableText(
                      "actrade.command.trade_accepted_you_can_open_inv", 
                      ACTextUtil.parse(new StringReader("{\"text\":\""+ oppositeSender.getEntityName() +"\", \"color\":\"yellow\"}")),
                      ACTradeCommand.getTextRawButton("Open TradeBox", "/trade open " + currentTradeUuid.toString())
                    ),
                    true
                    );
                    oppositeSender.sendMessage(
                      new TranslatableText(
                        "actrade.command.trade_accepted_you_can_open_inv",
                        ACTextUtil.parse(new StringReader("{\"text\":\""+ thisPlayer.getEntityName() +"\", \"color\":\"yellow\"}")),
                        ACTradeCommand.getTextRawButton("Open TradeBox", "/trade open " + currentTradeUuid.toString())
                      ),
                      false
                    );
                } else if (currentTrade.currentTradeState == Trade.TradeState.STARTED) {
                 System.out.print("[SENDER] %s already started");
                 System.out.println(oppositeSender.getEntityName());
                  thisPlayer.sendMessage(new TranslatableText("actrade.command.trade_already_started"), false);
                } else {
                  System.out.print("[SENDER] %s already finished");
                  System.out.println(oppositeSender.getEntityName());
                  thisPlayer.sendMessage(new TranslatableText("actrade.command.trade_already_finished"), false);
                  ACTrade.mapTrades.remove(currentTradeUuid);
                }
              }
            }
          }
        }
        
        return 1;
      }).build();
      trade.addChild(tradIdArg);
    }

    private static void registerOpenTradeInv (CommandNode<ServerCommandSource> root) {
		  LiteralCommandNode<ServerCommandSource> trade = CommandManager.literal("open").build();
      root.addChild(trade);

      ArgumentCommandNode<ServerCommandSource, UUID> tradIdArg = CommandManager.argument("tradeId", TradeArgumentType.trades(TradeState.STARTED)).executes(ctx -> {
        ServerPlayerEntity thisPlayer = ctx.getSource().getPlayer();
        UUID targetTradeUuid = TradeArgumentType.getTrade(ctx, "tradeId", thisPlayer.getUuid());

        Trade currentTrade = ACTrade.mapTrades.get(targetTradeUuid);
        if (currentTrade != null) {
          if (currentTrade.currentTradeState == TradeState.FINISH || currentTrade.currentTradeState == TradeState.PENDING) {
            thisPlayer.sendMessage(new TranslatableText("actrade.command.current_trade_hasnot_started"), false);
          } else {
            thisPlayer.closeScreenHandler();
            ServerPlayerEntity sp = currentTrade.areWeSender(thisPlayer.getUuid()) ? (ServerPlayerEntity)ctx.getSource().getWorld().getEntity(currentTrade.receiverUuid) : (ServerPlayerEntity)ctx.getSource().getWorld().getEntity(currentTrade.senderUuid);
            thisPlayer.openHandledScreen(new ACScreenProvider(targetTradeUuid, sp.getEntityName()));
            thisPlayer.sendMessage(new TranslatableText(
              "actrade.command.player_x_is_in_trade_box",
              ACTextUtil.parse(new StringReader("{\"text\":\""+ thisPlayer.getEntityName() +"\", \"color\":\"yellow\"}"))
            ), false);
          }
        } else {
          // Not found
        }
        return 1;
      }).build();
      trade.addChild(tradIdArg);
    }
    
    static Text getTradeText (int index, String playerName, Trade trade, CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
      if (trade.currentTradeState == TradeState.STARTED) {
        return new TranslatableText(
          "actrade.command.trade_text_list_element",
          Integer.toString(index),
          playerName,
          getTextRawButtonColor("Open", "/trade open " + trade.tradeUuid.toString(), "green"),
          getTextRawButtonColor("Close", "/trade close " + trade.tradeUuid.toString(), "red")
        );
      } else {
        return new TranslatableText(
          "actrade.command.trade_text_list_element",
          Integer.toString(index),
          playerName,
          getTextRawButtonColor("Accept", "/trade accept " + trade.tradeUuid.toString(), "blue"),
          getTextRawButtonColor("Reject", "/trade reject " + trade.tradeUuid.toString(), "red")
        );
      }
    }

    private static void registerList (CommandNode<ServerCommandSource> root) {
      // If no player argument -> reject all pending requests
      LiteralCommandNode<ServerCommandSource> trade = CommandManager.literal("list").executes(ctx -> {
        ServerPlayerEntity thisPlayer = ctx.getSource().getPlayer();
        ArrayList<Trade> newListOfTrade = new ArrayList<Trade>();
        
        for (Map.Entry<UUID, Trade> currentEntry : ACTrade.mapTrades.entrySet()) {
          Trade tempTrade = currentEntry.getValue();
          if (ACTradeUtil.playerDoesExistInTrade(thisPlayer.getUuid(), tempTrade))
            newListOfTrade.add(tempTrade);
        }

        int currentId = 0;
        thisPlayer.sendMessage(Text.of("List page : " + 1), false);
        java.util.Iterator<Trade> t = newListOfTrade.iterator();
        while(t.hasNext()) {
          Trade currentTrade = t.next();
          if (currentTrade.currentTradeState == TradeState.STARTED) {
            thisPlayer.sendMessage(new TranslatableText(
              "actrade.command.trade_text_list_element",
              Integer.toString(currentId),
              ACTradeUtil.areWeSender(currentTrade.tradeUuid, thisPlayer.getUuid()) ? ctx.getSource().getWorld().getEntity(currentTrade.receiverUuid).getEntityName() : ctx.getSource().getWorld().getEntity(currentTrade.senderUuid).getEntityName(),
              getTextRawButtonColor("Open", "/trade open " + currentTrade.tradeUuid.toString(), "green"),
              getTextRawButtonColor("Close", "/trade close " + currentTrade.tradeUuid.toString(), "red")
            ), false);
          } else {
            thisPlayer.sendMessage(new TranslatableText(
              "actrade.command.trade_text_list_element",
              Integer.toString(currentId),
              ACTradeUtil.areWeSender(currentTrade.tradeUuid, thisPlayer.getUuid()) ? ctx.getSource().getWorld().getEntity(currentTrade.receiverUuid).getEntityName() : ctx.getSource().getWorld().getEntity(currentTrade.senderUuid).getEntityName(),
              getTextRawButtonColor("Accept", "/trade accept " + currentTrade.tradeUuid.toString(), "blue"),
              getTextRawButtonColor("Reject", "/trade reject " + currentTrade.tradeUuid.toString(), "red")
            ), false);
          }
          currentId = currentId + 1;
        }
        

        return 1;
      }).build();
      root.addChild(trade);

      ArgumentCommandNode<ServerCommandSource, Integer> tradIdArg = CommandManager.argument("tradeId", IntegerArgumentType.integer()).executes(ctx -> {
        int targetTradeListInterger = IntegerArgumentType.getInteger(ctx, "tradeId");
        // Trade targetTrade = ACTrade.mapTrades.get(targetTradeUuid);
        ServerPlayerEntity thisPlayer = ctx.getSource().getPlayer();
        
        ArrayList<Trade> newListOfTrade = new ArrayList<Trade>();
        for (Map.Entry<UUID, Trade> currentEntry : ACTrade.mapTrades.entrySet()) {
          Trade tempTrade = currentEntry.getValue();
          if (ACTradeUtil.playerDoesExistInTrade(thisPlayer.getUuid(), tempTrade))
            newListOfTrade.add(tempTrade);
        }
        
        
        // newListOfTrade.forEach(action -> getTradeText(0, trade));

        if (targetTradeListInterger > 1 && newListOfTrade.size() < 4) {
        } else {
          new TranslatableText(
            "actrade.command.",
            newListOfTrade.get(targetTradeListInterger - 1 * 4 + 0),
            newListOfTrade.get(targetTradeListInterger - 1 * 4 + 1),
            newListOfTrade.get(targetTradeListInterger - 1 * 4 + 2),
            newListOfTrade.get(targetTradeListInterger - 1 * 4 + 3 )
          );
        }
        return 1;
      }).build();
      trade.addChild(tradIdArg);
    }

    private static void registerReject (CommandNode<ServerCommandSource> root) {
      // If no player argument -> reject all pending requests
      // Send
      // List :
      // -> PlayerName : Accept / Reject OR Open
    }

    // Activate trade system -> Enable receiving and sending trade events
    private static void registerActive (CommandNode<ServerCommandSource> root) {
      // 
    }

    private static void registerClose (CommandNode<ServerCommandSource> root) {
      LiteralCommandNode<ServerCommandSource> close = CommandManager.literal("close").requires(source -> source.hasPermissionLevel(1)).executes(ctx -> {
        ServerPlayerEntity thisPlayer = ctx.getSource().getPlayer();
        Trade tradeMarked = null;
        for (Entry<UUID, Trade> currentEntry : ACTrade.mapTrades.entrySet()) {
          if (ACTradeUtil.playerDoesExistInTrade(thisPlayer.getUuid(), currentEntry.getValue())){
            if (currentEntry.getValue().currentTradeState == TradeState.STARTED) {
              tradeMarked = currentEntry.getValue();
            }
          }
        }
        if (tradeMarked != null) {
          boolean areWeSender = tradeMarked.areWeSender(thisPlayer.getUuid());
          thisPlayer.sendMessage(new TranslatableText("actrade.command.trade_with_x_has_been_closed", getColoredText(thisPlayer.getEntityName(), "yellow")), false);
          ServerPlayerEntity sPlayerEntity = areWeSender ? (ServerPlayerEntity)ctx.getSource().getWorld().getEntity(tradeMarked.receiverUuid) : (ServerPlayerEntity)ctx.getSource().getWorld().getEntity(tradeMarked.receiverUuid);
          sPlayerEntity.sendMessage(new TranslatableText("actrade.command.trade_with_x_has_been_closed", getColoredText(thisPlayer.getEntityName(), "yellow")), false);
          ACTrade.mapTrades.remove(tradeMarked.tradeUuid);
        }
        return 1;
      }).build();
      root.addChild(close);

    }

    public static void init(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
      LiteralCommandNode<ServerCommandSource> root = CommandManager.literal("trade").requires(source -> source.hasPermissionLevel(1)).build();
      dispatcher.getRoot().addChild(root);
      if (!dedicated) {
        registerRequest(root);
        registerAccept(root);
        registerOpenTradeInv(root);
        registerList(root);
        registerClose(root);
      }
    }
    
    static Text getColoredText (String text, String color, boolean translatable) throws CommandSyntaxException {
      if (translatable) {
        return ACTextUtil.parse(new StringReader("{\"translate\":\""+ text +"\",\"color\":\""+ color +"\"}"));
      } else return ACTextUtil.parse(new StringReader("{\"text\":\""+ text +"\",\"color\":\""+ color +"\"}"));
    }

    static Text getColoredText (String text, String color) throws CommandSyntaxException {
      return getColoredText(text, color, false);
    }

    static Text getColoredTranslatableText (String text, String color) throws CommandSyntaxException {
      return getColoredText(text, color, true);
    }

    static Text getTextRawButton (String text, String commandText) throws CommandSyntaxException {
      return ACTextUtil.parse(new StringReader("{\"text\":\""+ text +"\",\"color\":\"blue\",\"clickEvent\":{\"action\":\"run_command\",\"\":\"blue\",\"value\":\""+ commandText +"\"}}"));
    }

    static Text getTextRawButtonColor (String text, String commandText, String color) throws CommandSyntaxException {
      return ACTextUtil.parse(new StringReader("{\"text\":\""+ text +"\",\"color\":\""+color+"\",\"clickEvent\":{\"action\":\"run_command\",\"\":\"blue\",\"value\":\""+ commandText +"\"}}"));
    }
    
    static Text getTextRawButtonTranslatable (String translateName, String commandText) throws CommandSyntaxException {
      return ACTextUtil.parse(new StringReader("{\"translate\":\""+ translateName +"\",\"color\":\"blue\",\"clickEvent\":{\"action\":\"run_command\",\"\":\"blue\",\"value\":\""+ commandText +"\"}}"));
    }
}
