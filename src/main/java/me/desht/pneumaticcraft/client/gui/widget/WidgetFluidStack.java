package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.function.Consumer;

public class WidgetFluidStack extends WidgetFluidFilter {
    private final IFluidTank tank;

    public WidgetFluidStack(int x, int y, IFluidTank tank, Consumer<WidgetFluidFilter> pressable) {
        super(x, y, pressable);
        this.tank = tank;
    }

    WidgetFluidStack(int x, int y, FluidStack stack, Consumer<WidgetFluidFilter> pressable) {
        super(x, y);
        tank = new FluidTank(stack.getAmount());
        tank.fill(stack, IFluidHandler.FluidAction.EXECUTE);
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTick) {
        fluid = tank.getFluid().getFluid();
        super.renderButton(mouseX, mouseY, partialTick);
        if (!tank.getFluid().isEmpty()) {
            int fluidAmount = tank.getFluidAmount() / 1000;
            String s = fluidAmount + "B";
            if (fluidAmount > 1) {
                FontRenderer fr = Minecraft.getInstance().fontRenderer;
                GlStateManager.translated(0, 0, 400);  // ensure amount is drawn in front of the fluid texture
                fr.drawStringWithShadow(s, x - fr.getStringWidth(s) + 17, y + 9, 0xFFFFFFFF);
                GlStateManager.translated(0, 0, -400);
            }
        }
    }
}
