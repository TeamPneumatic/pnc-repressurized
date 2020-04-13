package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.GuiAphorismTile;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAphorismTile;
import me.desht.pneumaticcraft.lib.BBConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class RenderAphorismTile extends TileEntityRenderer<TileEntityAphorismTile> {

    public RenderAphorismTile(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(TileEntityAphorismTile te, float v, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        matrixStack.push();

        matrixStack.translate(0.5, 1.5, 0.5);
        matrixStack.scale(1, -1, -1);
        RenderUtils.rotateMatrixForDirection(matrixStack, te.getRotation());
        matrixStack.translate(0, 1, 0.5 - BBConstants.APHORISM_TILE_THICKNESS - 0.01);

        FontRenderer fr = Minecraft.getInstance().getRenderManager().getFontRenderer();
        int fh = fr.FONT_HEIGHT;

        String[] textLines = te.getTextLines();
        int lineWidth = te.getMaxLineWidth();
        int lineHeight = fh * textLines.length;
        float textScale = Math.min(14 / 16F / lineWidth, 14 / 16F / lineHeight);
        matrixStack.scale(textScale, textScale, textScale);
        matrixStack.rotate(Vector3f.ZP.rotationDegrees(te.textRotation * 90));

        int editedLine = getEditedLine(te);

        for (int i = 0; i < textLines.length; i++) {
            String textLine = editedLine == i ? ">" + textLines[i] + "<" : textLines[i];
            float x = -fr.getStringWidth(textLine) / 2f;
            float y = -(textLines.length * fh) / 2 + i * fh + 1;
            fr.renderString(textLine, x, y, 0xFF000000, false, matrixStack.getLast().getMatrix(), buffer, false, 0, 15728880);
        }

        matrixStack.pop();
    }

    private int getEditedLine(TileEntityAphorismTile te) {
        if (Minecraft.getInstance().currentScreen instanceof GuiAphorismTile) {
            GuiAphorismTile gui = (GuiAphorismTile) Minecraft.getInstance().currentScreen;
            if (gui.tile == te && (gui.updateCounter & 0x0f) < 8) {
                return gui.cursorY;
            }
        }
        return -1;
    }
}
