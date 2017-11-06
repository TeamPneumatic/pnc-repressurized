package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketAphorismTileUpdate;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAphorismTile;
import me.desht.pneumaticcraft.common.util.DramaSplash;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatAllowedCharacters;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.List;

public class GuiAphorismTile extends GuiScreen {
    public final TileEntityAphorismTile tile;
    private String[] textLines;
    public int cursorY;
    public int cursorX;
    public int updateCounter;

    public GuiAphorismTile(TileEntityAphorismTile tile) {
        this.tile = tile;
        textLines = tile.getTextLines();
        if (ConfigHandler.client.aphorismDrama && textLines.length == 1 && textLines[0].equals("")) {
            List<String> l = PneumaticCraftUtils.convertStringIntoList(DramaSplash.getInstance().getSplash(), 20);
            tile.setTextLines(l.toArray(new String[0]));
        }
        NetworkHandler.sendToServer(new PacketAphorismTileUpdate(tile));
    }

    @Override
    public void updateScreen() {
        updateCounter++;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (Keyboard.isKeyDown(Keyboard.KEY_F1)) {
            showHelpScreen();
        }
    }

    private void showHelpScreen() {
        List<String> helpText = PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.aphorismTile.helpText"), 40);
        int boxWidth = 0;
        int boxHeight = helpText.size() * fontRenderer.FONT_HEIGHT;
        for (String s : helpText) {
            boxWidth = Math.max(boxWidth, fontRenderer.getStringWidth(s));
        }
        int x = (this.width - boxWidth)  / 2;
        int y = (this.height - boxHeight)  / 2;
        drawRect(x - 4, y - 4, x + boxWidth + 8, y + boxHeight + 8, 0x80000000);
        drawHorizontalLine(x - 4, x + boxWidth + 8, y - 4, 0xFF808080);
        drawHorizontalLine(x - 4, x + boxWidth + 8, y + boxHeight + 8, 0xFF808080);
        drawVerticalLine(x - 4, y - 4, y + boxHeight + 8, 0xFF808080);
        drawVerticalLine(x + boxWidth + 8, y - 4, y + boxHeight + 8, 0xFF808080);
        for (String s : helpText) {
            drawString(fontRenderer, s, x, y, 0xFFE0E0E0);
            y += fontRenderer.FONT_HEIGHT;
        }
    }

    @Override
    protected void keyTyped(char par1, int par2) throws IOException {
        if (par2 == Keyboard.KEY_ESCAPE) {
            NetworkHandler.sendToServer(new PacketAphorismTileUpdate(tile));
        } else if (par2 == Keyboard.KEY_LEFT || par2 == Keyboard.KEY_UP) {
            cursorY--;
            if (cursorY < 0) cursorY = textLines.length - 1;
        } else if (par2 == Keyboard.KEY_DOWN || par2 == Keyboard.KEY_NUMPADENTER) {
            cursorY++;
            if (cursorY >= textLines.length) cursorY = 0;
        } else if (par2 == Keyboard.KEY_RETURN) {
            cursorY++;
            textLines = ArrayUtils.add(textLines, cursorY, "");
        } else if (par2 == Keyboard.KEY_BACK) {
            if (textLines[cursorY].length() > 0) {
                textLines[cursorY] = textLines[cursorY].substring(0, textLines[cursorY].length() - 1);
                if (textLines[cursorY].endsWith("\u00a7")) {
                    textLines[cursorY] = textLines[cursorY].substring(0, textLines[cursorY].length() - 1);
                }
            } else if (textLines.length > 1) {
                textLines = ArrayUtils.remove(textLines, cursorY);
                cursorY--;
                if (cursorY < 0) cursorY = 0;
            }
        } else if (par2 == Keyboard.KEY_DELETE) {
            if (GuiScreen.isShiftKeyDown()) {
                textLines = new String[1];
                textLines[0] = "";
                cursorY = 0;
            } else {
                if (textLines.length > 1) {
                    textLines = ArrayUtils.remove(textLines, cursorY);
                    if (cursorY > textLines.length - 1)
                        cursorY = textLines.length - 1;
                }
            }
        } else if (ChatAllowedCharacters.isAllowedCharacter(par1)) {
            if (GuiScreen.isAltKeyDown()) {
                if (par1 >= 'a' && par1 <= 'f' || par1 >= 'l' && par1 <= 'o' || par1 == 'r' || par1 >= '0' && par1 <= '9') {
                    textLines[cursorY] = textLines[cursorY] + "\u00a7" + par1;
                }
            } else {
                textLines[cursorY] = textLines[cursorY] + par1;
            }
        }
        tile.setTextLines(textLines);
        super.keyTyped(par1, par2);
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
}
