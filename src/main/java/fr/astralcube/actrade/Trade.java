package fr.astralcube.actrade;

import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
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
    
    // Create a event when the trade has been expired -> Delete

    private final DefaultedList<ItemStack> senderInventory = DefaultedList.ofSize(9, ItemStack.EMPTY);
    private final DefaultedList<ItemStack> targetInventory = DefaultedList.ofSize(9, ItemStack.EMPTY);

    public Trade (UUID sId, UUID tId) {
        this.senderUuid = sId;
        this.receiverUuid = tId;
    }

    public void reset() {
        this.currentTradeState = TradeState.PENDING;
    }
    
}
