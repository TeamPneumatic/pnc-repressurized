package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.client.gui.GuiPneumaticContainerBase;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rectangle2d;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

public class GuiTabHandler implements IGlobalGuiHandler {
    @Override
    public Collection<Rectangle2d> getGuiExtraAreas() {
        if (Minecraft.getInstance().currentScreen instanceof GuiPneumaticContainerBase) {
            return ((GuiPneumaticContainerBase) Minecraft.getInstance().currentScreen).getTabRectangles();
        }
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse(double mouseX, double mouseY) {
        return null;
    }

}
