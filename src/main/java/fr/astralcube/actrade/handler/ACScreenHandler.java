package fr.astralcube.actrade.handler;

import java.util.UUID;

import fr.astralcube.actrade.ACTrade;
import fr.astralcube.actrade.util.ACTradeUtil;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerSyncHandler;
import net.minecraft.screen.slot.Slot;

public class ACScreenHandler extends ScreenHandler {
	private final UUID tradeUuid;
	private Inventory senderInventory;
	private Inventory receiverInventory;
    private TextFieldWidget searchBox;

    public ACScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, buf.readUuid());
    }
	
    public ACScreenHandler(int syncId, PlayerInventory playerInventory,  UUID tradeId) {
		super(ACTrade.AC_SCREEN, syncId);
        this.senderInventory =  new SimpleInventory(9);
        this.receiverInventory =  new SimpleInventory(9);
        // checkSize(ACTrade.mapTrades.get(tradeId).senderInventory, 9);
        // checkSize(ACTrade.mapTrades.get(tradeId).targetInventory, 9);
		this.tradeUuid = tradeId;

		ACTrade.trades.forEach(action -> {
			if (action.tradeUuid == tradeId) {
				this.senderInventory = new SimpleInventory(9);
				this.receiverInventory = new SimpleInventory(9);
			}
		});
        
		int m;
        int l;
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 3; ++l) {
                this.addSlot(new Slot(this.senderInventory, l + m * 3, 8 + l * 18, 18 + m * 18));
            }
        }
        
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 3; ++l) {
                this.addSlot(new Slot(this.receiverInventory, l + m * 3, 116 + l * 18, 18 + m * 18));
            }
        }

		//The player inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 101 + m * 18));
            }
        }

        //The player Hotbar
        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 159));
        }
	}



	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}

	 
    // Shift + Player Inv Slot
    @Override
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (ACTradeUtil.areWeSender(this.tradeUuid, player.getUuid())) {
                if (invSlot < this.receiverInventory.size()) {
                    if (!this.insertItem(originalStack, this.receiverInventory.size(), this.slots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.insertItem(originalStack, 0, this.receiverInventory.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (invSlot < this.senderInventory.size()) {
                    if (!this.insertItem(originalStack, this.senderInventory.size(), this.slots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.insertItem(originalStack, 0, this.senderInventory.size(), false)) {
                    return ItemStack.EMPTY;
                }
            }

 
            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
 
        return newStack;
    }


    @Override
    public void addListener(ScreenHandlerListener listener) {
        // TODO Auto-generated method stub
        super.addListener(listener);
    }

    @Override
    public void updateSyncHandler(ScreenHandlerSyncHandler handler) {
        super.updateSyncHandler(handler);
    }
}
