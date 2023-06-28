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
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;

public class WidgetFluidFilter extends AbstractWidget {
    final Consumer<WidgetFluidFilter> pressable;
    protected FluidStack fluidStack;

    public WidgetFluidFilter(int x, int y, Fluid fluid) {
        this(x, y, fluid, null);
    }

    public WidgetFluidFilter(int x, int y, Fluid fluid, Consumer<WidgetFluidFilter> pressable) {
        this(x, y, new FluidStack(fluid, 1000), pressable);
    }

    WidgetFluidFilter(int x, int y, FluidStack fluidStack, Consumer<WidgetFluidFilter> pressable) {
        super(x, y, 16, 16, Component.empty());
        this.pressable = pressable;
        this.fluidStack = fluidStack;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!fluidStack.isEmpty()) {
            GuiUtils.drawFluid(graphics, new Rect2i(getX(), getY(), 16, 16), new FluidStack(fluidStack, 1000), null);
            setTooltip(Tooltip.create(
                    new FluidStack(fluidStack, 1).getDisplayName().copy().append("\n")
                            .append(Component.literal(ModNameCache.getModName(fluidStack.getFluid()))
                                    .withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC)))
            );
        } else {
            setTooltip(null);
        }
    }

    public Fluid getFluid() {
        return fluidStack.getFluid();
    }

    public WidgetFluidFilter setFluid(Fluid fluid) {
        this.fluidStack = new FluidStack(fluid, 1000);
        return this;
    }

    @Override
    public void onClick(double x, double y) {
        super.onClick(x, y);

        if (pressable != null) pressable.accept(this);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }
}
