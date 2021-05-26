package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.client.gui.GuiPneumaticContainerBase;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rectangle2d;

import java.util.List;

public class GuiTabHandler implements IGuiContainerHandler<GuiPneumaticContainerBase<?,?>> {
    @Override
    public List<Rectangle2d> getGuiExtraAreas(GuiPneumaticContainerBase<?,?> containerScreen) {
        return containerScreen.getTabRectangles();
    }
}
