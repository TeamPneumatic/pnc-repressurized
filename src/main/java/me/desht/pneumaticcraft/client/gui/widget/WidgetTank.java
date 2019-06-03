package me.desht.pneumaticcraft.client.gui.widget;

import me.desht.pneumaticcraft.client.util.GuiUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.List;

/**
 * This class is derived from BluePower and edited by MineMaarten:
 * https://github.com/Qmunity/BluePower/blob/FluidCrafting/src/main/java/com/bluepowermod/client/gui/widget/WidgetTank.java
 */
public class WidgetTank extends WidgetBase {

    private final IFluidTank tank;

    public WidgetTank(int id, int x, int y, IFluidTank tank) {
        super(id, x, y, 16, 64);
        this.tank = tank;
    }

    public WidgetTank(int x, int y, FluidStack stack) {
        super(-1, x, y, 16, 64);
        tank = new FluidTank(stack, 16000);
    }

    public WidgetTank(int x, int y, int width, int height, FluidStack stack) {
        super(-1, x, y, width, height);
        tank = new FluidTank(stack, stack.amount);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        GlStateManager.disableLighting();
        GuiUtils.drawFluid(new Rectangle(x, y, getBounds().width, getBounds().height), getFluid(), getTank());

        // drawing a gauge rather than using the widget_tank texture since for some reason it doesn't work
        // https://github.com/desht/pnc-repressurized/issues/25
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 300);
        for (int i = 3; i < getBounds().height - 1; i += 4) {
            int width = (i - 3) % 20 == 0 ? 16 : 2;
            Gui.drawRect(x, y + i, x + width, y + i + 1, 0xFF2F2F2F);
        }
        GlStateManager.popMatrix();

//        GlStateManager.color(1, 1, 1, 1);
//        Minecraft.getMinecraft().getTextureManager().bindTexture(Textures.WIDGET_TANK);
//        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 64, 16, 64);
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTip, boolean shift) {
        Fluid fluid = null;
        int amt = 0;
        int capacity = 0;

        if (tank.getFluid() != null) {
            fluid = tank.getFluid().getFluid();
            amt = tank.getFluidAmount();
        }
        capacity = tank.getCapacity();

        if (fluid == null || amt == 0 || capacity == 0) {
            curTip.add(amt + "/" + capacity + " mb");
            curTip.add(TextFormatting.GRAY + I18n.format("gui.liquid.empty"));
        } else {
            curTip.add(amt + "/" + capacity + " mb");
            curTip.add(TextFormatting.GRAY + fluid.getLocalizedName(new FluidStack(fluid, amt)));
        }
    }

    public FluidStack getFluid() {
        return tank.getFluid();
    }

    @SideOnly(Side.CLIENT)
    public FluidTank getTank() {
        return (FluidTank) tank;
    }
}
