package fr.astralcube.actrade.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import fr.astralcube.actrade.ACTrade;
import fr.astralcube.actrade.client.ACTradeClient;
import fr.astralcube.actrade.handler.ACScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ACTradeScreen extends AbstractInventoryScreen<ACScreenHandler> {
	private static final Identifier TEXTURE = new Identifier("minecraft", "textures/gui/container/dispenser.png");
    
	public ACTradeScreen(ACScreenHandler acScreenHandler, PlayerInventory playerInventory, Text text) {
		super(acScreenHandler, playerInventory, text);
        this.playerInventoryTitleY = this.playerInventoryTitleY + 18;
        this.backgroundHeight = this.backgroundHeight + 17;
    }


    @Override
    protected void drawBackground(MatrixStack matrices, float var2, int var3, int var4) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, ACTradeClient.GUI);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
	}

	@Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }
	@Override
    protected void init() {
        super.init();
        // Center the title
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }
}
