package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.gui.GuiAphorismTile;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAphorismTile;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.BBConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

public class RenderAphorismTile extends TileEntitySpecialRenderer<TileEntityAphorismTile> {
    @Override
    public void render(TileEntityAphorismTile te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        GL11.glScalef(1.0F, -1F, -1F);
        PneumaticCraftUtils.rotateMatrixByMetadata(te.getBlockMetadata());
        GL11.glTranslatef(0, 1, 0.5F - BBConstants.APHORISM_TILE_THICKNESS - 0.01F);
        String[] textLines = te.getTextLines();
        int lineWidth = getMaxLineWidth(textLines);  // TODO we don't need to calculate this every single tick
        int lineHeight = 10 * textLines.length;
        float textScale = Math.min(14 / 16F / lineWidth, 14 / 16F / lineHeight);
        GL11.glScalef(textScale, textScale, textScale);
        GL11.glRotatef(te.textRotation * 90, 0, 0, 1);
        int editedLine = -1;

        if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiAphorismTile) {
            GuiAphorismTile gui = (GuiAphorismTile) FMLClientHandler.instance().getClient().currentScreen;
            if (gui.tile == te && (gui.updateCounter & 0x0f) < 8) {
                editedLine = gui.cursorY;
            }
        }

        FontRenderer f = Minecraft.getMinecraft().getRenderManager().getFontRenderer();
        for (int i = 0; i < textLines.length; i++) {
            String textLine = textLines[i];
            if (editedLine == i) textLine = ">" + textLine + "<";
            f.drawString(TextFormatting.ITALIC + textLine, -f.getStringWidth(textLine) / 2, -(textLines.length * 10) / 2 + i * 10 + 1, 0xFF000000);
        }

        GL11.glPopMatrix();
    }

    private int getMaxLineWidth(String[] textList) {
        int maxLength = 0;
        for (String string : textList) {
            int stringWidth = Minecraft.getMinecraft().getRenderManager().getFontRenderer().getStringWidth(string);
            if (stringWidth > maxLength) {
                maxLength = stringWidth;
            }
        }
        return maxLength;
    }
}
