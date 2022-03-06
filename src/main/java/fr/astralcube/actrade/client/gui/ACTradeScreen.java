package fr.astralcube.actrade.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import fr.astralcube.actrade.client.ACTradeClient;
import fr.astralcube.actrade.handler.ACScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ACTradeScreen extends AbstractInventoryScreen<ACScreenHandler> {
    
	public ACTradeScreen(ACScreenHandler acScreen, PlayerInventory playerInventory, Text text) {
		super(acScreen, playerInventory, text);
	}


	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
	}

    @Override
    protected void drawBackground(MatrixStack matrices, float var2, int var3, int var4) {
		int u = this.x;
		int v = (this.height - this.backgroundHeight) / 2;
		
		RenderSystem.setShaderTexture(0, ACTradeClient.GUI);
		this.drawTexture(matrices, u, v, 0, 0, this.backgroundWidth, this.backgroundWidth);
	}
}
