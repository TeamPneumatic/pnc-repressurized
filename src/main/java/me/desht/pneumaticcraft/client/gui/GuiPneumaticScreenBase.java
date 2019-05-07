package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.client.gui.widget.IWidgetListener;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.commons.lang3.text.WordUtils;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class GuiPneumaticScreenBase extends GuiScreen implements IWidgetListener {

    protected final List<IGuiWidget> widgets = new ArrayList<>();
    public int guiLeft, guiTop, xSize, ySize;

    @Override
    public void initGui() {
        super.initGui();
        widgets.clear();
        guiLeft = width / 2 - xSize / 2;
        guiTop = height / 2 - ySize / 2;
    }

    public void addWidgets(Iterable<IGuiWidget> widgets) {
        for (IGuiWidget widget : widgets) {
            addWidget(widget);
        }
    }

    public void addWidget(IGuiWidget widget) {
        widgets.add(widget);
        widget.setListener(this);
    }

    protected void addLabel(String text, int x, int y) {
        addWidget(new WidgetLabel(x, y, text));
    }

    public void removeWidget(IGuiWidget widget) {
        widgets.remove(widget);
    }

    protected abstract ResourceLocation getTexture();

    @Override
    public void drawScreen(int x, int y, float partialTicks) {
        GlStateManager.color(1f, 1f, 1f, 1.0f);
        if (getTexture() != null) {
            FMLClientHandler.instance().getClient().getTextureManager().bindTexture(getTexture());
            drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        }
        super.drawScreen(x, y, partialTicks);

        for (IGuiWidget widget : widgets) {
            widget.render(x, y, partialTicks);
        }
        for (IGuiWidget widget : widgets) {
            widget.postRender(x, y, partialTicks);
        }
        GlStateManager.enableTexture2D();
        GlStateManager.color(0.25f, 0.25f, 0.25f, 1.0f);

        List<String> tooltip = new ArrayList<>();
        for (Object obj : buttonList) {
            if (obj instanceof GuiButtonSpecial) {
                GuiButtonSpecial button = (GuiButtonSpecial) obj;
                if (button.x < x && button.x + button.getWidth() > x && button.y < y && button.y + button.getHeight() > y) {
                    button.getTooltip(tooltip);
                }
            }
        }
        boolean shift = PneumaticCraftRepressurized.proxy.isSneakingInGui();
        for (IGuiWidget widget : widgets) {
            if (widget.getBounds().contains(x, y)) widget.addTooltip(x, y, tooltip, shift);
        }
        if (!tooltip.isEmpty()) {
            List<String> localizedTooltip = new ArrayList<>();
            for (String line : tooltip) {
                String localizedLine = I18n.format(line);
                for (String wrappedLine : localizedLine.split("\\\\n")) {
                    String[] lines = WordUtils.wrap(wrappedLine, 50).split(System.getProperty("line.separator"));
                    localizedTooltip.addAll(Arrays.asList(lines));
                }
            }
            drawHoveringText(localizedTooltip, x, y, fontRenderer);
        }
        GlStateManager.color(0.25f, 0.25f, 0.25f, 1.0f);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        // new arraylist creation necessary to avoid a comod exception
        for (IGuiWidget widget : new ArrayList<>(widgets)) {
            if (widget.getBounds().contains(mouseX, mouseY)) {
                widget.onMouseClicked(mouseX, mouseY, mouseButton);
            } else {
                widget.onMouseClickedOutsideBounds(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    protected void keyTyped(char key, int keyCode) throws IOException {
        if (keyCode == 1) {
            super.keyTyped(key, keyCode);
        } else {
            for (IGuiWidget widget : widgets) {
                widget.onKey(key, keyCode);
            }
        }
    }

    @Override
    public void actionPerformed(IGuiWidget widget) {
        if (widget instanceof IGuiAnimatedStat) {
            boolean leftSided = ((IGuiAnimatedStat) widget).isLeftSided();
            for (IGuiWidget w : widgets) {
                if (w instanceof IGuiAnimatedStat) {
                    IGuiAnimatedStat stat = (IGuiAnimatedStat) w;
                    if (widget != stat && stat.isLeftSided() == leftSided) {//when the stat is on the same side, close it.
                        stat.closeWindow();
                    }
                }
            }
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        for (IGuiWidget widget : widgets) {
            widget.update();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        for (IGuiWidget widget : widgets) {
            widget.handleMouseInput();
        }
    }

    @Override
    public void onKeyTyped(IGuiWidget widget) {
    }

    @Override
    public void setWorldAndResolution(Minecraft par1Minecraft, int par2, int par3) {
        widgets.clear();
        super.setWorldAndResolution(par1Minecraft, par2, par3);
    }

    public GuiButton getButtonFromRectangle(int buttonID, Rectangle buttonSize, String buttonText) {
        return new GuiButton(buttonID, buttonSize.x, buttonSize.y, buttonSize.width, buttonSize.height, buttonText);
    }

    public GuiButtonSpecial getInvisibleButtonFromRectangle(int buttonID, Rectangle buttonSize) {
        return new GuiButtonSpecial(buttonID, buttonSize.x, buttonSize.y, buttonSize.width, buttonSize.height, "");
    }

    public GuiTextField getTextFieldFromRectangle(Rectangle textFieldSize) {
        return new GuiTextField(-1, fontRenderer, textFieldSize.x, textFieldSize.y, textFieldSize.width, textFieldSize.height);
    }

}
