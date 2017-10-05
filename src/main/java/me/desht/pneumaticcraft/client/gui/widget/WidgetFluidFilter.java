package me.desht.pneumaticcraft.client.gui.widget;

import me.desht.pneumaticcraft.client.gui.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;

public class WidgetFluidFilter extends WidgetBase {
    protected Fluid fluid;

    public WidgetFluidFilter(int id, int x, int y) {
        super(id, x, y, 16, 16);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        if (fluid != null) {
            GuiUtils.drawFluid(new Rectangle(x, y, 16, 16), new FluidStack(fluid, 1000), null);
        }
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTip, boolean shiftPressed) {
        if (fluid != null) curTip.add(fluid.getLocalizedName(new FluidStack(fluid, 1)));
    }

    public WidgetFluidFilter setFluid(Fluid fluid) {
        this.fluid = fluid;
        return this;
    }

    public Fluid getFluid() {
        return fluid;
    }
}
