package pneumaticCraft.common.progwidgets;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import pneumaticCraft.lib.Textures;

public class ProgWidgetLiquidFilter extends ProgWidget{
    private Fluid fluid;

    @Override
    public boolean hasStepInput(){
        return false;
    }

    @Override
    public Class<? extends IProgWidget> returnType(){
        return ProgWidgetLiquidFilter.class;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetLiquidFilter.class};
    }

    @Override
    public String getWidgetString(){
        return "liquidFilter";
    }

    @Override
    public String getGuiTabText(){
        return "bla";
    }

    @Override
    public int getGuiTabColor(){
        return 0xFFFFFFFF;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_LIQUID_FILTER;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        fluid = FluidRegistry.getFluid(tag.getString("fluid"));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        if(fluid != null) tag.setString("fluid", fluid.getName());
    }

    public boolean isLiquidValid(Fluid fluid){
        return this.fluid == null || fluid == this.fluid;
    }

    public static boolean isLiquidValid(Fluid fluid, IProgWidget mainWidget, int filterIndex){
        ProgWidgetLiquidFilter widget = (ProgWidgetLiquidFilter)mainWidget.getConnectedParameters()[mainWidget.getParameters().length + filterIndex];
        while(widget != null) {
            if(!widget.isLiquidValid(fluid)) return false;
            widget = (ProgWidgetLiquidFilter)widget.getConnectedParameters()[0];
        }
        widget = (ProgWidgetLiquidFilter)mainWidget.getConnectedParameters()[filterIndex];
        if(widget == null) return true;
        while(widget != null) {
            if(widget.isLiquidValid(fluid)) return true;
            widget = (ProgWidgetLiquidFilter)widget.getConnectedParameters()[0];
        }
        return false;
    }

}
