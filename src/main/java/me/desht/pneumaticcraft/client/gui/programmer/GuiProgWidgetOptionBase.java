package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiPneumaticScreenBase;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketProgrammerUpdate;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiProgWidgetOptionBase<Widget extends IProgWidget> extends GuiPneumaticScreenBase {
    protected Widget widget;
    protected GuiProgrammer guiProgrammer;

    public GuiProgWidgetOptionBase(Widget widget, GuiProgrammer guiProgrammer) {
        this.widget = widget;
        this.guiProgrammer = guiProgrammer;
        xSize = 183;
        ySize = 202;
    }

    @Override
    public void keyTyped(char key, int keyCode) throws IOException {
        super.keyTyped(key, keyCode);
        if (keyCode == 1) {
            onGuiClosed();
            if (guiProgrammer != null) {
                NetworkHandler.sendToServer(new PacketProgrammerUpdate(guiProgrammer.te));
                mc.displayGuiScreen(guiProgrammer);
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        String title = I18n.format("programmingPuzzle." + widget.getWidgetString() + ".name");
        addLabel(title, width / 2 - fontRenderer.getStringWidth(title) / 2, guiTop + 5);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_WIDGET_OPTIONS;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
