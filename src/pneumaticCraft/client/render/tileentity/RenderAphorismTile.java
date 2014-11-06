package pneumaticCraft.client.render.tileentity;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.GuiAphorismTile;
import pneumaticCraft.common.tileentity.TileEntityAphorismTile;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.BBConstants;
import cpw.mods.fml.client.FMLClientHandler;

public class RenderAphorismTile extends TileEntitySpecialRenderer{
    @Override
    public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f){
        TileEntityAphorismTile tile = (TileEntityAphorismTile)tileentity;
        GL11.glPushMatrix(); // start
        GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
        GL11.glScalef(1.0F, -1F, -1F);
        PneumaticCraftUtils.rotateMatrixByMetadata(tile.getBlockMetadata());
        GL11.glTranslatef(0, 1, 0.5F - BBConstants.APHORISM_TILE_THICKNESS - 0.01F);
        String[] textLines = tile.getTextLines();
        int lineWidth = getMaxLineWidth(textLines);
        int lineHeight = 10 * textLines.length;
        float textScale = Math.min(14 / 16F / lineWidth, 14 / 16F / lineHeight);
        GL11.glScalef(textScale, textScale, textScale);
        GL11.glRotatef(tile.textRotation * 90, 0, 0, 1);
        int editedLine = -1;
        if(FMLClientHandler.instance().getClient().currentScreen instanceof GuiAphorismTile) {
            GuiAphorismTile gui = (GuiAphorismTile)FMLClientHandler.instance().getClient().currentScreen;
            if(gui.tile == tile && gui.updateCounter % 12 < 6) {
                editedLine = gui.cursorY;
            }
        }

        for(int i = 0; i < textLines.length; i++) {
            String textLine = textLines[i];
            if(editedLine == i) textLine = ">" + textLine + "<";
            RenderManager.instance.getFontRenderer().drawString(EnumChatFormatting.ITALIC + textLine, -RenderManager.instance.getFontRenderer().getStringWidth(textLine) / 2, -(textLines.length * 10) / 2 + i * 10 + 1, 0xFF000000);
        }

        GL11.glPopMatrix(); // end

    }

    private int getMaxLineWidth(String[] textList){
        int maxLength = 0;
        for(String string : textList) {
            int stringWidth = RenderManager.instance.getFontRenderer().getStringWidth(string);
            if(stringWidth > maxLength) {
                maxLength = stringWidth;
            }
        }
        return maxLength;
    }

}
