package fr.astralcube.actrade.handler;

import java.util.UUID;

import fr.astralcube.actrade.ACTrade;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class ACScreenHandler extends ScreenHandler {
	private final UUID tradeUuid;
	private Inventory senderInventory;
	private Inventory targetInventory;
	
	public ACScreenHandler(int syncId, PlayerInventory inv,  UUID tradeId) {
		super(ACTrade.AC_SCREEN, syncId);
		this.tradeUuid = tradeId;
		ACTrade.trades.forEach(action -> {
			if (action.tradeUuid == tradeId) {
				this.senderInventory = new SimpleInventory(9);
				this.targetInventory = new SimpleInventory(9);
			}
		});

		int m;
        int l;
		//The player inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(inv, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
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
            if (invSlot < this.targetInventory.size()) {
                if (!this.insertItem(originalStack, this.targetInventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.targetInventory.size(), false)) {
                return ItemStack.EMPTY;
            }
 
            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
 
        return newStack;
    }
}
