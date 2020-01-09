package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiPneumaticScreenBase;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketProgrammerUpdate;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class GuiProgWidgetOptionBase<P extends IProgWidget> extends GuiPneumaticScreenBase {
    protected final P progWidget;
    protected final GuiProgrammer guiProgrammer;

    GuiProgWidgetOptionBase(P progWidget, GuiProgrammer guiProgrammer) {
        super(new TranslationTextComponent(progWidget.getTranslationKey()));

        this.progWidget = progWidget;
        this.guiProgrammer = guiProgrammer;
        xSize = 183;
        ySize = 202;
    }

    @Override
    public void init() {
        super.init();
        String title = TextFormatting.UNDERLINE + I18n.format(progWidget.getTranslationKey());
        addLabel(title, width / 2 - font.getStringWidth(title) / 2, guiTop + 5);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        if (guiProgrammer != null) {
            NetworkHandler.sendToServer(new PacketProgrammerUpdate(guiProgrammer.te));
            minecraft.displayGuiScreen(guiProgrammer);
        } else {
            super.onClose();
        }
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_WIDGET_OPTIONS;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
