package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
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
        this.fluid = tank.getFluid().getFluid();
    }

    WidgetFluidStack(int x, int y, FluidStack stack, Consumer<WidgetFluidFilter> pressable) {
        this(x, y, makeTank(stack), pressable);
    }

    private static IFluidTank makeTank(FluidStack stack) {
        IFluidTank tank = new FluidTank(stack.getAmount());
        tank.fill(stack, IFluidHandler.FluidAction.EXECUTE);
        return tank;
    }

    public FluidStack getStack() {
        return tank.getFluid();
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTick) {
        super.renderButton(mouseX, mouseY, partialTick);

        if (!tank.getFluid().isEmpty()) {
            int fluidAmount = tank.getFluidAmount() / 1000;
            if (fluidAmount > 1) {
                FontRenderer fr = Minecraft.getInstance().fontRenderer;
                GlStateManager.translated(0, 0, 400);  // ensure amount is drawn in front of the fluid texture
                String s = fluidAmount + "B";
                fr.drawStringWithShadow(s, x - fr.getStringWidth(s) + 17, y + 9, 0xFFFFFFFF);
                GlStateManager.translated(0, 0, -400);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.clicked(mouseX, mouseY)) {
            boolean shift = Screen.hasShiftDown();
            switch (button) {
                case 0:  // left-click: drain 1000mB (or halve with Shift held)
                    tank.drain(shift ? tank.getFluidAmount() / 2 : 1000, IFluidHandler.FluidAction.EXECUTE);
                    if (tank.getFluidAmount() < 1000) {
                        tank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                    }
                    break;
                case 1:  // right-click: add 1000mB (or double with Shift held)
                    tank.fill(new FluidStack(tank.getFluid().getFluid(), shift ? tank.getFluidAmount() : 1000), IFluidHandler.FluidAction.EXECUTE);
                    break;
                case 2:  // middle-click: clear slot
                    tank.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
                    break;

            }
            fluid = tank.getFluid().getFluid();
            if (pressable != null) pressable.accept(this);
            return true;
        } else {
            return false;
        }
    }
}
