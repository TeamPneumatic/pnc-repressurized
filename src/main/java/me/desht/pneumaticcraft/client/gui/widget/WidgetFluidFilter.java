package me.desht.pneumaticcraft.client.gui.widget;

import me.desht.pneumaticcraft.client.util.GuiUtils;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Consumer;

public class WidgetFluidFilter extends Widget implements ITooltipProvider {
    protected final Consumer<WidgetFluidFilter> pressable;
    protected Fluid fluid;

    public WidgetFluidFilter(int x, int y) {
        this(x, y, null);
    }

    public WidgetFluidFilter(int x, int y, Consumer<WidgetFluidFilter> pressable) {
        super(x, y, 16, 16, "");
        this.pressable = pressable;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTick) {
        if (fluid != null && fluid != Fluids.EMPTY) {
            GuiUtils.drawFluid(new Rectangle2d(x, y, 16, 16), new FluidStack(fluid, 1000), null);
        }
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<String> curTip, boolean shiftPressed) {
        if (fluid != null && fluid != Fluids.EMPTY) curTip.add(new FluidStack(fluid, 1).getDisplayName().getFormattedText());
    }

    public WidgetFluidFilter setFluid(Fluid fluid) {
        this.fluid = fluid;
        return this;
    }

    public Fluid getFluid() {
        return fluid;
    }

    @Override
    public void onClick(double x, double y) {
        super.onClick(x, y);

        if (pressable != null) pressable.accept(this);
    }
}
