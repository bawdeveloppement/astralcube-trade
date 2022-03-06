package fr.astralcube.actrade.commands;

import java.util.Map;
import java.util.UUID;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import fr.astralcube.actrade.ACTrade;
import fr.astralcube.actrade.Trade;
import fr.astralcube.actrade.Trade.TradeState;
import fr.astralcube.actrade.handler.ACScreenProvider;
import fr.astralcube.actrade.util.ACTextUtil;
import fr.astralcube.actrade.util.TradeArgumentType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;


public class ACTradeCommand {
    
    private static void registerRequest(CommandNode<ServerCommandSource> root) {
		  LiteralCommandNode<ServerCommandSource> trade = CommandManager.literal("request").build();
        root.addChild(trade);
		  ArgumentCommandNode<ServerCommandSource, EntitySelector> playerArg = CommandManager.argument("player", EntityArgumentType.player()).executes(ctx -> {
        ServerPlayerEntity receiverPlayer = EntityArgumentType.getPlayer(ctx, "player");
        ServerPlayerEntity senderPlayer = ctx.getSource().getPlayer();

        UUID senderPlayerUuid = senderPlayer.getUuid();
        boolean tradeExist = false;
        // If the receiverPlayer is not us
        if (receiverPlayer.getUuid() != senderPlayerUuid) {          
          for (Map.Entry<UUID, Trade> t : ACTrade.mapTrades.entrySet()) {
            UUID tempUuid = t.getKey();
            Trade tempTrade = t.getValue();
            // If a trade with the receiver and sender already exist
            if (tempTrade.senderUuid == senderPlayerUuid && tempTrade.receiverUuid == receiverPlayer.getUuid()) {
              // A TRADE EXIST AND HAS FINISHED
              if (tempTrade.currentTradeState == TradeState.FINISH) {
                ACTrade.mapTrades.get(t.getKey()).reset();
                ctx.getSource().sendFeedback(new TranslatableText("actrade.command.request_already_sent"), false);
                receiverPlayer.sendMessage(new TranslatableText("actrade.command.request", ACTextUtil.parse(new StringReader("{\"text\":\""+ ctx.getSource().getPlayer().getEntityName() +"\", \"color\":\"yellow\"}")), ACTextUtil.parse(new StringReader("{\"translate\":\"actrade.command.accept_trade_text_button\",\"color\":\"blue\",\"clickEvent\":{\"action\":\"run_command\",\"\":\"blue\",\"value\":\"/trade accept " + tempUuid +"\"}}"))), false);
              }
              // A TRADE EXIST AND HAS ALREADY STARTED
              else if (tempTrade.currentTradeState == TradeState.STARTED) {
                ctx.getSource().sendFeedback(new TranslatableText("actrade.command.request_already_sent_open_inv"), false);
              }
              // THE REQUEST IS ALREADY PENDING
              else {
                ctx.getSource().sendFeedback(new TranslatableText("actrade.command.the_request_is_already_pending"), false);
              }
              tradeExist = true;
            }
          }
          
          if (!tradeExist) {
            Trade newTrade = new Trade(senderPlayerUuid, receiverPlayer.getUuid());
            UUID newUUID = UUID.randomUUID();
            ACTrade.mapTrades.put(newUUID, newTrade);
            receiverPlayer.sendMessage(new TranslatableText("actrade.command.request", ACTextUtil.parse(new StringReader("{\"text\":\""+ ctx.getSource().getPlayer().getEntityName() +"\", \"color\":\"yellow\"}")), ACTextUtil.parse(new StringReader("{\"translate\":\"actrade.command.accept_trade_text_button\",\"color\":\"blue\",\"clickEvent\":{\"action\":\"run_command\",\"\":\"blue\",\"value\":\"/trade accept " + newUUID.toString() +"\"}}"))), false);
          }

          return 1;
        } else {
          senderPlayer.sendMessage(new TranslatableText("actrade.command.cannot_send_trade_oursef"), false);
          return 0;
        }

      }).build();
      trade.addChild(playerArg);
    }

    private static void registerAccept (CommandNode<ServerCommandSource> root) {
		  LiteralCommandNode<ServerCommandSource> trade = CommandManager.literal("accept").build();
      root.addChild(trade);

      ArgumentCommandNode<ServerCommandSource, UUID> tradIdArg = CommandManager.argument("tradeId", TradeArgumentType.uuid()).executes(ctx -> {
        UUID tradeUUID = TradeArgumentType.getUuid(ctx, "tradeId");
        Trade targetTrade = ACTrade.mapTrades.get(tradeUUID);

        for (Map.Entry<UUID, Trade> t : ACTrade.mapTrades.entrySet()) {
          UUID currentTradeUuid = t.getKey();
          Trade tempTrade = t.getValue();
          ServerPlayerEntity sP = (ServerPlayerEntity)ctx.getSource().getWorld().getEntity(tempTrade.senderUuid);
          // If the senderPlayer is the same as our and the trades is already started -> PLAYER_ALREADY_TRADING
          if (currentTradeUuid != tradeUUID && t.getValue().currentTradeState == TradeState.STARTED && t.getValue().senderUuid == targetTrade.senderUuid) {
            ctx.getSource().sendFeedback(new TranslatableText("actrade.command.player_already_trading"), false);
            ACTrade.mapTrades.remove(tradeUUID);
            break;
          }
          // 
          else if (t.getKey() == tradeUUID) {
            switch(tempTrade.currentTradeState.toString()) {
              case "PENDING":
                tempTrade.currentTradeState = TradeState.STARTED;
                ACTrade.mapTrades.replace(tradeUUID, tempTrade);
                ctx.getSource().sendFeedback(new TranslatableText("actrade.command.open_trade_inventory"), false);
                sP.sendMessage( new TranslatableText("actrade.command.open_trade_inventory", ctx.getSource().getPlayer()), false);
                break;
              case "STARTED":
                ctx.getSource().sendFeedback(new TranslatableText("actrade.command.already_accepted_open_inv"), false);
                break;
              case "FINISH":
                ctx.getSource().sendFeedback(new TranslatableText("actrade.command.trade_finished"), false);
                ACTrade.mapTrades.remove(tradeUUID);
                break;
              default:
                break;
            }
          }
        }

        return 1;
      }).build();
      trade.addChild(tradIdArg);
    }

    private static void registerOpenTradeInv (CommandNode<ServerCommandSource> root) {
		  LiteralCommandNode<ServerCommandSource> trade = CommandManager.literal("open").executes(ctx -> {
        ServerPlayerEntity senderPlayer = ctx.getSource().getPlayer();

        for (Map.Entry<UUID, Trade> t : ACTrade.mapTrades.entrySet()) {
          Trade tempTrade = t.getValue();
          if (tempTrade.currentTradeState == TradeState.STARTED && ( tempTrade.senderUuid == senderPlayer.getUuid() || tempTrade.receiverUuid == senderPlayer.getUuid())) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(0);
            ctx.getSource().getPlayer().closeScreenHandler();
            ctx.getSource().getPlayer().openHandledScreen(new ACScreenProvider(t.getKey()));
            // if client enable this TODO: config
            ctx.getSource().sendFeedback(new TranslatableText("actrade.command.player_x_openned_trade_inv"), false);
          } else {
            ctx.getSource().sendFeedback(new TranslatableText("actrade.command.cannot_open_trade_not_found"), false);
          }
        }
        
        return 1;
      }).build();
      root.addChild(trade);
    }
    
    
    private static void registerReject (CommandNode<ServerCommandSource> root) {
      // If no player argument -> reject all pending requests
    }

    // Activate trade system -> Enable receiving and sending trade events
    private static void registerActive (CommandNode<ServerCommandSource> root) {
      // 
    }

    public static void init(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
      LiteralCommandNode<ServerCommandSource> root = CommandManager.literal("trade").requires(source -> source.hasPermissionLevel(1)).build();
      dispatcher.getRoot().addChild(root);
      if (!dedicated) {
        registerRequest(root);
        registerAccept(root);
        registerOpenTradeInv(root);
      }
    }
}
