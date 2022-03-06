package fr.astralcube.actrade;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.astralcube.actrade.commands.ACTradeCommand;
import fr.astralcube.actrade.handler.ACNetworkHandler;
import fr.astralcube.actrade.handler.ACScreenHandler;



public class ACTrade implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MODID = "actrade";
	public static final Logger LOGGER = LoggerFactory.getLogger("actrade");
	public static ArrayList<Trade> trades = new ArrayList<Trade>();
	public static Map<UUID, Trade> mapTrades = new HashMap<UUID, Trade>();

	public static final Identifier AC_TRADE_SCREEN_ID = new Identifier("actrade", "ac_trade_screen");
	public static final ScreenHandlerType<ACScreenHandler> AC_SCREEN = ScreenHandlerRegistry.registerExtended(AC_TRADE_SCREEN_ID, (syncId, inv, buf) -> new ACScreenHandler(syncId, inv, buf.readUuid()));

    // public static final Identifier PLAY_PARTICLE_PACKET_ID = new Identifier("actrade", "accept");

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		CommandRegistrationCallback.EVENT.register(ACTradeCommand::init);
		ServerPlayNetworking.registerGlobalReceiver(AC_TRADE_SCREEN_ID, ACNetworkHandler::switchScreen);
		
		LOGGER.info("Hello Fabric world!");
	}

	
}
