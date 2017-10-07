package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorCaller;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderElevatorCaller extends TileEntitySpecialRenderer<TileEntityElevatorCaller> {
    @Override
    public void render(TileEntityElevatorCaller te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        Tessellator tess = Tessellator.getInstance();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 1.5, z + 0.5);
        GlStateManager.scale(1.0F, -1F, -1F);
        PneumaticCraftUtils.rotateMatrixByMetadata(te.getBlockMetadata());
        GlStateManager.translate(-1, 0, -1);

        FontRenderer fontRenderer = Minecraft.getMinecraft().getRenderManager().getFontRenderer();
        for (TileEntityElevatorCaller.ElevatorButton button : te.getFloors()) {
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.color(button.red, button.green, button.blue, 1F);
            BufferBuilder bufferBuilder = tess.getBuffer();
            bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            bufferBuilder.pos(button.posX + 0.5D, button.posY + 0.5D, 0.499D).endVertex();
            bufferBuilder.pos(button.posX + 0.5D, button.posY + button.height + 0.5D, 0.499D).endVertex();
            bufferBuilder.pos(button.posX + button.width + 0.5D, button.posY + button.height + 0.5D, 0.499D).endVertex();
            bufferBuilder.pos(button.posX + button.width + 0.5D, button.posY + 0.5D, 0.499D).endVertex();
            tess.draw();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();

            GlStateManager.pushMatrix();
            GlStateManager.translate(button.posX + 0.5D, button.posY + 0.5D, 0.498);
            GlStateManager.translate(button.width / 2, button.height / 2, 0);
            float textScale = Math.min((float)button.width / 10F, (float)button.height / 10F);
            GlStateManager.scale(textScale, textScale, textScale);
            fontRenderer.drawString(button.buttonText, -fontRenderer.getStringWidth(button.buttonText) / 2, -fontRenderer.FONT_HEIGHT / 2, 0xFF000000);
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }
}
