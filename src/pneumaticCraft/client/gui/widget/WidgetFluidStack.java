package pneumaticCraft.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;

public class WidgetFluidStack extends WidgetFluidFilter{
    private final IFluidTank tank;

    public WidgetFluidStack(int id, int x, int y, IFluidTank tank){
        super(id, x, y);
        this.tank = tank;
    }

    public WidgetFluidStack(int id, int x, int y, FluidStack stack){
        super(id, x, y);
        tank = new FluidTank(stack.amount);
        tank.fill(stack, true);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick){
        fluid = tank.getFluid() != null ? tank.getFluid().getFluid() : null;
        super.render(mouseX, mouseY, partialTick);
        if(fluid != null) {
            int fluidAmount = tank.getFluidAmount() / 1000;
            String s = fluidAmount + "B";
            if(fluidAmount > 1) Minecraft.getMinecraft().fontRenderer.drawString(s, x - Minecraft.getMinecraft().fontRenderer.getStringWidth(s) + 17, y + 9, 0xFFFFFFFF, true);
        }
    }
}
