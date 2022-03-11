package fr.astralcube.actrade.client;

import fr.astralcube.actrade.ACTrade;
import fr.astralcube.actrade.client.gui.ACTradeScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.util.Identifier;

public class ACTradeClient implements ClientModInitializer {
	public static final Identifier GUI = new Identifier(ACTrade.MODID, "textures/gui/gui.png");

    @Override
    public void onInitializeClient() {
		ScreenRegistry.register(ACTrade.AC_SCREEN, ACTradeScreen::new);
    }
}
