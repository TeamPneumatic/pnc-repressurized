/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.PNCForgeSlider;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.block.entity.utility.AphorismTileBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketAphorismTileUpdate;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.drama.DramaGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class AphorismTileScreen extends Screen {
    private static final int PANEL_HEIGHT = 88;
    public final AphorismTileBlockEntity blockEntity;
    private String[] textLines;
    public int cursorY;
    public int cursorX;
    public int updateCounter;
    private ItemSearcherScreen itemSearchGui;
    private int panelWidth;

    public AphorismTileScreen(AphorismTileBlockEntity blockEntity, boolean placing) {
        super(new ItemStack(ModBlocks.APHORISM_TILE.get()).getHoverName());

        this.blockEntity = blockEntity;
        textLines = blockEntity.getTextLines();
        blockEntity.needMaxLineWidthRecalc();
        if (ConfigHelper.client().general.aphorismDrama.get() && placing && textLines.length == 1 && textLines[0].isEmpty()) {
            List<String> l = PneumaticCraftUtils.splitString(DramaGenerator.generateDrama(), 20);
            blockEntity.setTextLines(l.toArray(new String[0]));
            textLines = blockEntity.getTextLines();
            NetworkHandler.sendToServer(PacketAphorismTileUpdate.forBlockEntity(blockEntity));
        }

        Pair<Integer,Integer> cursor = blockEntity.getCursorPos();
        cursorX = cursor.getLeft();
        cursorY = cursor.getRight();
    }

    @Override
    public void init() {
        int yPos = (height - PANEL_HEIGHT) / 2;
        addRenderableWidget(new PNCForgeSlider(5, yPos, 90, 16,  Component.literal("Margin: "), Component.empty(),
                0, 9, blockEntity.getMarginSize(), true, slider -> blockEntity.setMarginSize(slider.getValueInt())));

        WidgetCheckBox cb;
        WidgetButtonExtended itemButton, rsButton;

        addRenderableWidget(cb = new WidgetCheckBox(5, yPos + 22, 0xFFFFFF, xlate("pneumaticcraft.gui.logistics_frame.invisible"), b -> blockEntity.setInvisible(b.checked))
                .setChecked(blockEntity.isInvisible()));

        addRenderableWidget(new WidgetLabel(5, yPos + 38, xlate("pneumaticcraft.gui.aphorismTile.insert"), 0xFFFFFF80));

        Component txt = xlate("pneumaticcraft.gui.aphorismTile.insertItem");
        addRenderableWidget(itemButton = new WidgetButtonExtended(10, yPos + 50, font.width(txt) + 10, 18, txt, b -> openItemSelector()));

        txt = xlate("pneumaticcraft.gui.redstone");
        addRenderableWidget(rsButton = new WidgetButtonExtended(10, yPos + 70, font.width(txt) + 10, 18, txt, b -> {
            textLines[cursorY] = textLines[cursorY] + "{redstone}";
            blockEntity.setTextLines(textLines);
        }));

        panelWidth = Math.max(100, Math.max(cb.getWidth(), Math.max(rsButton.getWidth(), itemButton.getWidth()))) + 5;
        if (itemSearchGui != null && !itemSearchGui.getSearchStack().isEmpty()) {
            ResourceLocation regName = PneumaticCraftUtils.getRegistryName(itemSearchGui.getSearchStack().getItem()).orElseThrow();
            String text = "{item:" + regName + "}";
            textLines[cursorY] = text;
            cursorX = text.length();
            blockEntity.setTextLines(textLines);
            itemSearchGui = null;
        }
    }

    private void openItemSelector() {
        ClientUtils.openContainerGui(ModMenuTypes.ITEM_SEARCHER.get(), Component.literal("Searcher"));
        if (minecraft.screen instanceof ItemSearcherScreen scr) {
            itemSearchGui = scr;
        }
    }

    public static void openGui(AphorismTileBlockEntity te, boolean placing) {
        if (te != null) {
            Minecraft.getInstance().setScreen(new AphorismTileScreen(te, placing));
        }
    }

    @Override
    public void tick() {
        updateCounter++;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        GuiUtils.drawPanel(graphics, -5, (height - PANEL_HEIGHT) / 2, PANEL_HEIGHT, panelWidth);

        for (Renderable renderable : this.renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTicks);
        }
        if (ClientUtils.isKeyDown(GLFW.GLFW_KEY_F1)) {
            GuiUtils.showPopupHelpScreen(graphics, this, font,
                    GuiUtils.xlateAndSplit("pneumaticcraft.gui.aphorismTile.helpText"));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean updateTE = false;

        String curLine = textLines[cursorY];
        float p = curLine.isEmpty() ? 0f : (float)cursorX / curLine.length();

        switch (keyCode) {
            case GLFW.GLFW_KEY_ESCAPE -> NetworkHandler.sendToServer(PacketAphorismTileUpdate.forBlockEntity(blockEntity));
            case GLFW.GLFW_KEY_UP -> {
                cursorY--;
                if (cursorY < 0) cursorY = textLines.length - 1;
                cursorX = (int) (textLines[cursorY].length() * p);
            }
            case GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_KP_ENTER -> {
                cursorY++;
                if (cursorY >= textLines.length) cursorY = 0;
                cursorX = (int) (textLines[cursorY].length() * p);
            }
            case GLFW.GLFW_KEY_LEFT -> {
                if (cursorX > 0) {
                    cursorX--;
                    if (cursorX > 0 && curLine.charAt(cursorX - 1) == '§') {
                        cursorX--;
                    }
                } else if (cursorX == 0 && cursorY > 0) {
                    cursorY--;
                    cursorX = textLines[cursorY].length();
                }
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                if (cursorX < curLine.length()) {
                    cursorX++;
                    if (cursorX < curLine.length() && curLine.charAt(cursorX - 1) == '§') cursorX++;
                } else if (cursorY < textLines.length - 1) {
                    cursorY++;
                    cursorX = 0;
                }
            }
            case GLFW.GLFW_KEY_HOME -> cursorX = 0;
            case GLFW.GLFW_KEY_END -> cursorX = curLine.length();
            case GLFW.GLFW_KEY_ENTER -> {
                cursorY++;
                int oldCursorX = cursorX;
                cursorX = 0;
                textLines = insertLine(textLines[cursorY - 1].substring(oldCursorX), cursorY);
                textLines[cursorY - 1] = textLines[cursorY - 1].substring(0, oldCursorX);
                updateTE = true;
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (curLine.length() > 0 && cursorX > 0) {
                    // delete behind cursor, move cursor back
                    textLines[cursorY] = curLine.substring(0, cursorX - 1) + curLine.substring(Math.min(curLine.length(), cursorX));
                    cursorX--;
                    if (cursorX > 0 && textLines[cursorY].charAt(cursorX - 1) == '§') {
                        textLines[cursorY] = textLines[cursorY].substring(0, cursorX - 1) + textLines[cursorY].substring(Math.min(textLines[cursorY].length(), cursorX));
                        cursorX--;
                    }
                } else if (textLines.length > 1 && cursorY > 0) {
                    // join this line to previous line
                    String line = textLines[cursorY];
                    textLines = ArrayUtils.remove(textLines, cursorY);
                    if (cursorY > 0) cursorY--;
                    cursorX = textLines[cursorY].length();
                    textLines[cursorY] = textLines[cursorY] + line;
                }
                updateTE = true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (Screen.hasShiftDown()) {
                    // clear all
                    textLines = new String[1];
                    textLines[0] = "";
                    cursorX = cursorY = 0;
                } else if (Screen.hasAltDown()) {
                    // delete this line
                    if (textLines.length > 1) {
                        textLines = ArrayUtils.remove(textLines, cursorY);
                        if (cursorY > textLines.length - 1)
                            cursorY = textLines.length - 1;
                    } else {
                        textLines[0] = "";
                        cursorX = 0;
                    }
                } else {
                    if (curLine.length() > 0 && cursorX < curLine.length()) {
                        // delete ahead of cursor
                        int n = curLine.charAt(cursorX) == '§' && cursorX < curLine.length() - 1 ? 2 : 1;
                        textLines[cursorY] = curLine.substring(0, cursorX) + curLine.substring(cursorX + n);
                    } else if (cursorY < textLines.length - 1) {
                        // join following line to this
                        textLines[cursorY] = curLine + textLines[cursorY + 1];
                        textLines = ArrayUtils.remove(textLines, cursorY + 1);
                    }
                }
                if (cursorX > textLines[cursorY].length()) cursorX = (int) (textLines[cursorY].length() * p);
                updateTE = true;
            }
        }
        blockEntity.setCursorPos(cursorX, cursorY);
        if (updateTE) blockEntity.setTextLines(textLines);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char ch, int keyCode) {
        if (StringUtil.isAllowedChatCharacter(ch)) {
            if (Screen.hasAltDown()) {
                if (ch >= 'a' && ch <= 'f' || ch >= 'l' && ch <= 'o' || ch == 'r' || ch >= '0' && ch <= '9') {
                    textLines[cursorY] = textLines[cursorY].substring(0, cursorX) + "§" + ch + textLines[cursorY].substring(cursorX);
                    cursorX += 2;
                }
            } else {
                textLines[cursorY] = textLines[cursorY].substring(0, cursorX) + ch + textLines[cursorY].substring(cursorX);
                cursorX++;
            }
            blockEntity.setTextLines(textLines);
        }
        return super.charTyped(ch, keyCode);
    }

    @Override
    public void removed() {
        blockEntity.needMaxLineWidthRecalc();
        super.removed();
    }

    private String[] insertLine(String line, int pos) {
        String[] newLines = new String[textLines.length + 1];

        newLines[pos] = line;
        if (pos > 0) {
            System.arraycopy(textLines, 0, newLines, 0, pos);
        }
        if (pos < textLines.length) {
            System.arraycopy(textLines, pos, newLines, pos + 1, textLines.length - pos);
        }
        return newLines;
    }
}
