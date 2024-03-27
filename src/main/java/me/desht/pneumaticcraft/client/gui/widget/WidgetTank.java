/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.gui.widget;

import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.thirdparty.ModNameCache;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * This class is derived from BluePower and edited by MineMaarten:
 * https://github.com/Qmunity/BluePower/blob/FluidCrafting/src/main/java/com/bluepowermod/client/gui/widget/WidgetTank.java
 */
public class WidgetTank extends AbstractWidget {
    private final IFluidTank tank;

    public WidgetTank(int x, int y, IFluidTank tank) {
        super(x, y, 16, 64, Component.empty());
        this.tank = tank;
    }

    public WidgetTank(int x, int y, FluidStack stack) {
        this(x, y, makeTank(stack, 160000));
    }

    public WidgetTank(int x, int y, int width, int height, FluidStack stack) {
        super(x, y, width, height, Component.empty());
        this.tank = makeTank(stack, stack.getAmount());
    }

    public WidgetTank(int x, int y, int width, int height, FluidStack stack, int capacity) {
        super(x, y, width, height, Component.empty());
        this.tank = makeTank(stack, capacity);
    }

    private static FluidTank makeTank(FluidStack stack, int capacity) {
        FluidTank tank = new FluidTank(capacity);
        tank.fill(stack, IFluidHandler.FluidAction.EXECUTE);
        return tank;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX(), y = getY();
        GuiUtils.drawFluid(graphics, new Rect2i(x, y, width, height), getFluid(), getTank());

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);
        for (int i = 3; i < height - 1; i += 4) {
            int width = (i - 3) % 20 == 0 ? 16 : 2;
            graphics.fill(x, y + i, x + width, y + i + 1, 0xFF2F2F2F);
        }
        graphics.pose().popPose();

        if (isHovered) {
            graphics.renderTooltip(Minecraft.getInstance().font, makeTooltip(), Optional.empty(), mouseX, mouseY);
        }
    }

    public List<Component> makeTooltip() {
        Fluid fluid = tank.getFluid().getFluid();
        String amt = NumberFormat.getNumberInstance(Locale.getDefault()).format(tank.getFluidAmount());
        String capacity = NumberFormat.getNumberInstance(Locale.getDefault()).format(tank.getCapacity());
        List<Component> l = new ArrayList<>();
        l.add(Component.literal(amt + " / " + capacity + " mB"));
        if (fluid == Fluids.EMPTY || tank.getCapacity() == 0 || tank.getFluidAmount() == 0) {
            l.add(xlate("pneumaticcraft.gui.misc.empty").withStyle(ChatFormatting.GRAY));
        } else {
            l.add(new FluidStack(fluid, tank.getFluidAmount()).getDisplayName().copy().withStyle(ChatFormatting.GRAY));
            l.add(Component.literal(ModNameCache.getModName(fluid)).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
        }
        return l;
    }

    public FluidStack getFluid() {
        return tank.getFluid();
    }

    public IFluidTank getTank() {
        return tank;
    }

    public void setFluid(FluidStack fluidStack) {
        if (fluidStack.getFluid() != tank.getFluid().getFluid()) {
            tank.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
            tank.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
        } else if (fluidStack.getAmount() > tank.getFluidAmount()) {
            tank.fill(new FluidStack(fluidStack.getFluid(), fluidStack.getAmount() - tank.getFluidAmount()), IFluidHandler.FluidAction.EXECUTE);
        } else if (fluidStack.getAmount() < tank.getFluidAmount()) {
            tank.drain(tank.getFluidAmount() - fluidStack.getAmount(), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }
}
