package fr.astralcube.actrade.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;


import fr.astralcube.actrade.ACTrade;
import fr.astralcube.actrade.Trade;
import fr.astralcube.actrade.Trade.TradeState;

public class ACTradeUtil {
    public static Collection<String> getTradesUUIDOfPlayer(UUID targetPlayer,  Map<UUID, Trade> trades) {
        Collection<String> tradeOfPlayer = new HashSet<String>();
        for (Map.Entry<UUID, Trade> trade : trades.entrySet()) {
            Trade tempTrade = trade.getValue();
            if (ACTradeUtil.playerDoesExistInTrade(targetPlayer, tempTrade)) {
                tradeOfPlayer.add(trade.getKey().toString());
            }
        }
        return tradeOfPlayer;
    }
    
    public static boolean areWeSender (UUID tradeUUID, UUID playerUUID) {
        return ACTrade.mapTrades.get(tradeUUID).senderUuid == playerUUID;
      }
      
    public static boolean playerDoesExistInTrade( UUID playerUUID, Trade trade) {
        boolean doesExist = false;
        if (trade.senderUuid == playerUUID || trade.receiverUuid == playerUUID ) doesExist = true;
        return doesExist;
      }
  
  
    public static boolean isAlreadyInOtherTradeThan (UUID playerUUID, UUID tradeUUID) {
        boolean isInOtherTrade = false;
  
        for (Map.Entry<UUID, Trade> currentEntry : ACTrade.mapTrades.entrySet()) {
          if (currentEntry.getKey() != tradeUUID && ACTradeUtil.playerDoesExistInTrade(playerUUID, currentEntry.getValue())) {
            if (currentEntry.getValue().currentTradeState == TradeState.STARTED) isInOtherTrade = true;
          }
        }
        return isInOtherTrade;
    }
  
}
