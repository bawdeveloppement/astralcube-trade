package fr.astralcube.actrade;

import java.util.Date;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class Trade {
    public UUID senderUuid;
    public UUID receiverUuid;

    public static enum TradeState {
        PENDING,
        STARTED,
        FINISH,
    }

    public TradeState currentTradeState = TradeState.PENDING;

    // Not used
    public UUID tradeUuid = UUID.randomUUID();
    public Date acceptDate = new Date();
    
    // Create a event when the trade has been expired -> Delete

    public final DefaultedList<ItemStack> senderInventory = DefaultedList.ofSize(9, ItemStack.EMPTY);
    public final DefaultedList<ItemStack> targetInventory = DefaultedList.ofSize(9, ItemStack.EMPTY);

    public Trade (UUID thisUuid, UUID sId, UUID tId) {
        this.tradeUuid = thisUuid;
        this.senderUuid = sId;
        this.receiverUuid = tId;
    }

    public void reset() {
        this.currentTradeState = TradeState.PENDING;
    }

    public boolean areWeSender (UUID playerUUID) {
        return ACTrade.mapTrades.get(this.tradeUuid).senderUuid == playerUUID;
    }
    
}
