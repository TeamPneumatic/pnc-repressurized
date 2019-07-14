package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorCaller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderElevatorCaller extends TileEntityRenderer<TileEntityElevatorCaller> {
    @Override
    public void render(TileEntityElevatorCaller te, double x, double y, double z, float partialTicks, int destroyStage) {
        Tessellator tess = Tessellator.getInstance();
        GlStateManager.pushMatrix();
        GlStateManager.translated(x + 0.5, y + 1.5, z + 0.5);
        GlStateManager.scaled(1.0F, -1F, -1F);
        RenderUtils.rotateMatrixByMetadata(te.getRotation());
        GlStateManager.translated(-1, 0, -1);

        FontRenderer fontRenderer = Minecraft.getInstance().getRenderManager().getFontRenderer();
        for (TileEntityElevatorCaller.ElevatorButton button : te.getFloors()) {
            GlStateManager.disableTexture();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.color4f(button.red, button.green, button.blue, 1F);
            BufferBuilder bufferBuilder = tess.getBuffer();
            bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            bufferBuilder.pos(button.posX + 0.5D, button.posY + 0.5D, 0.499D).endVertex();
            bufferBuilder.pos(button.posX + 0.5D, button.posY + button.height + 0.5D, 0.499D).endVertex();
            bufferBuilder.pos(button.posX + button.width + 0.5D, button.posY + button.height + 0.5D, 0.499D).endVertex();
            bufferBuilder.pos(button.posX + button.width + 0.5D, button.posY + 0.5D, 0.499D).endVertex();
            tess.draw();
            GlStateManager.enableTexture();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();

            GlStateManager.pushMatrix();
            GlStateManager.translated(button.posX + 0.5D, button.posY + 0.5D, 0.498);
            GlStateManager.translated(button.width / 2, button.height / 2, 0);
            float textScale = Math.min((float)button.width / 10F, (float)button.height / 10F);
            GlStateManager.scaled(textScale, textScale, textScale);
            fontRenderer.drawString(button.buttonText, -fontRenderer.getStringWidth(button.buttonText) / 2f, -fontRenderer.FONT_HEIGHT / 2f, 0xFF000000);
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }
}
