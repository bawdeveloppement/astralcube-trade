package fr.astralcube.actrade.handler;

import java.util.UUID;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class ACScreenProvider implements ExtendedScreenHandlerFactory {
	private final UUID tradeUuid;
	
	public ACScreenProvider(final UUID tradeUuid) {
		this.tradeUuid = tradeUuid;
	}
	
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		return new ACScreenHandler(syncId, inv, this.tradeUuid);
	}
	
	@Override
	public Text getDisplayName() {
		return new TranslatableText("playerex.gui.page.attributes.title");
	}
	
	@Override
	public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
		buf.writeUuid(this.tradeUuid);
	}
}
