package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.render.area.AreaRenderManager;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiProgWidgetAreaShow<P extends IProgWidget> extends GuiProgWidgetOptionBase<P> {

    public GuiProgWidgetAreaShow(P progWidget, GuiProgrammer guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        if (displayShowAreaButtons()) {
            addButton(new WidgetCheckBox(guiLeft + 8, guiTop + ySize - (font.lineHeight + 8), 0x404040,
                    xlate("pneumaticcraft.gui.programmer.button.showArea"), this::previewArea)
                    .setChecked(AreaRenderManager.getInstance().isShowing(guiProgrammer.te)));
        }
    }

    protected void previewArea(WidgetCheckBox button) {
        guiProgrammer.te.previewArea(button.checked ? progWidget : null);
    }

    public boolean displayShowAreaButtons() {
        return true;
    }
}
