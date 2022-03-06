package fr.astralcube.actrade.handler;

import java.util.UUID;

import fr.astralcube.actrade.ACTrade;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ACNetworkHandler {
    
	public static final Identifier SCREEN = new Identifier(ACTrade.MODID, "screen");
	
	public static void switchScreen(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		UUID tradeUuid = buf.readUuid();
		
		server.execute(() -> {
			if(player != null) {
                player.closeScreenHandler();
                player.openHandledScreen(new ACScreenProvider(tradeUuid));
			}
		});
	}
}
