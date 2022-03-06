package fr.astralcube.actrade.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import fr.astralcube.actrade.Trade;

public class ACTradeUtil {
    public static Collection<String> getTradesUUIDOfPlayer(String targetPlayer,  Map<UUID, Trade> trades) {
        Collection<String> tradeOfPlayer = new HashSet<String>();
        for (Map.Entry<UUID, Trade> trade : trades.entrySet()) {
            Trade tempTrade = trade.getValue();
            tradeOfPlayer.add(trade.getKey().toString());
        }
        return tradeOfPlayer;
    }    
}
