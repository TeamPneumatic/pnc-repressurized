package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorCaller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderElevatorCaller extends TileEntityRenderer<TileEntityElevatorCaller> {

    private static final double Z_OFFSET = 0.499D;

    public RenderElevatorCaller(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(TileEntityElevatorCaller te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.push();
        matrixStackIn.translate(0.5, 1.5, 0.5);
        matrixStackIn.scale(1f, -1f, -1f);
        RenderUtils.rotateMatrixForDirection(matrixStackIn, te.getRotation());
        matrixStackIn.translate(-1, 0, -1);

        // todo lighting?

        FontRenderer fontRenderer = Minecraft.getInstance().getRenderManager().getFontRenderer();
        for (TileEntityElevatorCaller.ElevatorButton button : te.getFloors()) {

        }
    }

    @Override
    public void render(TileEntityElevatorCaller te, double x, double y, double z, float partialTicks, int destroyStage) {
        Tessellator tess = Tessellator.getInstance();
        GlStateManager.pushMatrix();
        GlStateManager.translated(x + 0.5, y + 1.5, z + 0.5);
        GlStateManager.scaled(1.0F, -1F, -1F);
        RenderUtils.rotateMatrixForDirection(matrixStack, te.getRotation());
        GlStateManager.translated(-1, 0, -1);

        // need this or it'll render black
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 240f, 240f);

        FontRenderer fontRenderer = Minecraft.getInstance().getRenderManager().getFontRenderer();
        for (TileEntityElevatorCaller.ElevatorButton button : te.getFloors()) {
            GlStateManager.disableTexture();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.color4f(button.red, button.green, button.blue, 1F);
            BufferBuilder bufferBuilder = tess.getBuffer();
            bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            bufferBuilder.pos(button.posX + 0.5D, button.posY + 0.5D, Z_OFFSET).endVertex();
            bufferBuilder.pos(button.posX + 0.5D, button.posY + button.height + 0.5D, Z_OFFSET).endVertex();
            bufferBuilder.pos(button.posX + button.width + 0.5D, button.posY + button.height + 0.5D, Z_OFFSET).endVertex();
            bufferBuilder.pos(button.posX + button.width + 0.5D, button.posY + 0.5D, Z_OFFSET).endVertex();
            tess.draw();
            GlStateManager.enableTexture();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();

            GlStateManager.pushMatrix();
            GlStateManager.translated(button.posX + 0.5D, button.posY + 0.5D, 0.498);
            GlStateManager.translated(button.width / 2, button.height / 2, 0);
            float textScale = Math.min((float)button.width / 10F, (float)button.height / 10F);
            GlStateManager.scalef(textScale, textScale, textScale);
            fontRenderer.drawString(button.buttonText, -fontRenderer.getStringWidth(button.buttonText) / 2f, -fontRenderer.FONT_HEIGHT / 2f, 0xFF000000);
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }
}
