package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.AreaShowManager;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.common.progwidgets.IAreaProvider;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;

public class GuiProgWidgetAreaShow<P extends IProgWidget> extends GuiProgWidgetOptionBase<P> {

    public GuiProgWidgetAreaShow(P progWidget, GuiProgrammer guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        if (displayShowAreaButtons() && progWidget instanceof IAreaProvider) {
            addButton(new WidgetButtonExtended(guiLeft + xSize / 2 - 50, guiTop + 150, 100, 20,
                    I18n.format("gui.programmer.button.showArea"), this::previewArea));
            if (AreaShowManager.getInstance().isShowing(guiProgrammer.te))
                addButton(new WidgetButtonExtended(guiLeft + xSize / 2 - 50, guiTop + 175, 100, 20,
                        I18n.format("gui.programmer.button.stopShowingArea"), this::stopPreviewing));
        }
    }

    protected void previewArea(Button button) {
        if (!AreaShowManager.getInstance().isShowing(guiProgrammer.te))
            addButton(new WidgetButtonExtended(guiLeft + xSize / 2 - 50, guiTop + 175, 100, 20, I18n.format("gui.programmer.button.stopShowingArea"), this::stopPreviewing));
        guiProgrammer.te.previewArea(progWidget.getX(), progWidget.getY());
    }

    private void stopPreviewing(Button button) {
        AreaShowManager.getInstance().removeHandlers(guiProgrammer.te);
        buttons.remove(button);
    }

    public boolean displayShowAreaButtons() {
        return true;
    }
}
