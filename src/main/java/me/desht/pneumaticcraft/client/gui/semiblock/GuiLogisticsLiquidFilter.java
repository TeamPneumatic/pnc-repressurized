package me.desht.pneumaticcraft.client.gui.semiblock;

import me.desht.pneumaticcraft.client.gui.programmer.GuiProgWidgetLiquidFilter;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetLiquidFilter;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fluids.Fluid;

import java.io.IOException;

public class GuiLogisticsLiquidFilter extends GuiProgWidgetLiquidFilter {
    private final GuiScreen parentScreen;

    public GuiLogisticsLiquidFilter(GuiScreen parentScreen) {
        super(new ProgWidgetLiquidFilter(), null);
        this.parentScreen = parentScreen;
    }

    public Fluid getFilter() {
        return widget.getFluid();
    }

    public void setFilter(Fluid fluid) {
        widget.setFluid(fluid);
    }

    @Override
    public void keyTyped(char key, int keyCode) throws IOException {
        super.keyTyped(key, keyCode);
        if (keyCode == 1) {
            mc.displayGuiScreen(parentScreen);
        }
    }
}
