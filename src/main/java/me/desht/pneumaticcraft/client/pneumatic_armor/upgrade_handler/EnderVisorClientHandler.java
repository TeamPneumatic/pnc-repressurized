package me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.EnderVisorHandler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class EnderVisorClientHandler extends IArmorUpgradeClientHandler.SimpleToggleableHandler<EnderVisorHandler> {
    public EnderVisorClientHandler() {
        super(CommonUpgradeHandlers.enderVisorHandler);
    }

    public static class PumpkinLayer implements LayeredDraw.Layer {
        private static final ResourceLocation PUMPKIN_OVERLAY = ResourceLocation.parse("textures/misc/pumpkinblur.png");

        @Override
        public void render(GuiGraphics graphics, DeltaTracker pDeltaTracker) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer();
            if (!handler.upgradeUsable(CommonUpgradeHandlers.enderVisorHandler, true)) {
               return;
            }

            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);
            RenderSystem.setShaderTexture(0, PUMPKIN_OVERLAY);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferbuilder.addVertex(0.0F, graphics.guiWidth(), -90.0F).setUv(0.0F, 1.0F);
            bufferbuilder.addVertex(graphics.guiWidth(), graphics.guiHeight(), -90.0F).setUv(1.0F, 1.0F);
            bufferbuilder.addVertex(graphics.guiWidth(), 0.0F, -90.0F).setUv(1.0F, 0.0F);
            bufferbuilder.addVertex(0.0F, 0.0F, -90.0F).setUv(0.0F, 0.0F);
            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
