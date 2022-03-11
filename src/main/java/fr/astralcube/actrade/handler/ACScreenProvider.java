package fr.astralcube.actrade.handler;

import java.util.UUID;

import fr.astralcube.actrade.ACTrade;
import fr.astralcube.actrade.Trade;
import fr.astralcube.actrade.util.ImplementedInventory;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents.StartTracking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;

public class ACScreenProvider implements ExtendedScreenHandlerFactory, ImplementedInventory {
	private final UUID tradeUuid;
	private String playerName;

	public ACScreenProvider(final UUID tradeUuid, String playerName) {
		this.tradeUuid = tradeUuid;
		this.playerName = playerName;
	}
	
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		return new ACScreenHandler(syncId, inv, this.tradeUuid);
	}
	
	@Override
	public Text getDisplayName() {
		return new TranslatableText("actrade.gui.trade.screenTitle", playerName);
	}
	
	@Override
	public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
		buf.writeUuid(this.tradeUuid);
	}

	@Override
	public void onClose(PlayerEntity player) {
		// TODO Auto-generated method stub
		ImplementedInventory.super.onClose(player);
	}

	@Override
	public DefaultedList<ItemStack> getItems() {
		// TODO Auto-generated method stub
		return null;
	}
}
