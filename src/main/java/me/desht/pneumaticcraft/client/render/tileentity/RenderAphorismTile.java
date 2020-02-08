package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.gui.GuiAphorismTile;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAphorismTile;
import me.desht.pneumaticcraft.lib.BBConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;

public class RenderAphorismTile extends TileEntityRenderer<TileEntityAphorismTile> {

    @Override
    public void render(TileEntityAphorismTile te, double x, double y, double z, float partialTicks, int destroyStage) {
        GlStateManager.pushMatrix();
        GlStateManager.translated((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        GlStateManager.scaled(1.0F, -1F, -1F);
        RenderUtils.rotateMatrixForDirection(te.getRotation());
        GlStateManager.translated(0, 1, 0.5F - BBConstants.APHORISM_TILE_THICKNESS - 0.01F);

        String[] textLines = te.getTextLines();
        int lineWidth = te.getMaxLineWidth();
        int lineHeight = 10 * textLines.length;
        float textScale = Math.min(14 / 16F / lineWidth, 14 / 16F / lineHeight);
        GlStateManager.scaled(textScale, textScale, textScale);
        GlStateManager.rotated(te.textRotation * 90, 0, 0, 1);

        int editedLine = -1;
        if (Minecraft.getInstance().currentScreen instanceof GuiAphorismTile) {
            GuiAphorismTile gui = (GuiAphorismTile) Minecraft.getInstance().currentScreen;
            if (gui.tile == te) {
                editedLine = gui.cursorY;
            }
        }

        FontRenderer f = Minecraft.getInstance().getRenderManager().getFontRenderer();
        int h = f.FONT_HEIGHT;
        for (int i = 0; i < textLines.length; i++) {
            String textLine = textLines[i];
            if (editedLine == i) textLine = ">" + textLine + "<";
            f.drawString(textLine, -f.getStringWidth(textLine) / 2f, -(textLines.length * h) / 2 + i * h + 1, 0xFF000000);
        }

        GlStateManager.popMatrix();
    }
}
