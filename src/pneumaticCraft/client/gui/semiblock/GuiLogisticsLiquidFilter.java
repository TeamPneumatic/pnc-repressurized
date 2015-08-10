package pneumaticCraft.client.gui.semiblock;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fluids.Fluid;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetLiquidFilter;
import pneumaticCraft.common.progwidgets.ProgWidgetLiquidFilter;

public class GuiLogisticsLiquidFilter extends GuiProgWidgetLiquidFilter{
    private final GuiScreen parentScreen;

    public GuiLogisticsLiquidFilter(GuiScreen parentScreen){
        super(new ProgWidgetLiquidFilter(), null);
        this.parentScreen = parentScreen;
    }

    public Fluid getFilter(){
        return widget.getFluid();
    }

    public void setFilter(Fluid fluid){
        widget.setFluid(fluid);
    }

    @Override
    public void keyTyped(char key, int keyCode){
        super.keyTyped(key, keyCode);
        if(keyCode == 1) {
            mc.displayGuiScreen(parentScreen);
        }
    }
}
