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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.Consumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class WidgetFluidStack extends AbstractWidget {
    private static final float TEXT_SCALE = 0.5f;
    final Consumer<WidgetFluidStack> pressable;
    private FluidStack fluidStack;

    private boolean adjustable = false;
    private boolean showAmount = false;

    public WidgetFluidStack(int x, int y, Fluid fluid, Consumer<WidgetFluidStack> pressable) {
        this(x, y, new FluidStack(fluid, FluidType.BUCKET_VOLUME), pressable);
    }

    public WidgetFluidStack(int x, int y, FluidStack fluidStack, Consumer<WidgetFluidStack> pressable) {
        super(x, y, 16, 16, Component.empty());
        this.pressable = pressable;
        this.fluidStack = fluidStack;
    }

    public WidgetFluidStack setAdjustable() {
        this.adjustable = true;
        return this;
    }

    public WidgetFluidStack setShowAmount() {
        this.showAmount = true;
        return this;
    }

    public FluidStack getFluidStack() {
        return fluidStack;
    }

    public WidgetFluidStack setFluidStack(FluidStack fluidStack) {
        this.fluidStack = fluidStack;
        return this;
    }

    public Fluid getFluid() {
        return fluidStack.getFluid();
    }

    public WidgetFluidStack setFluid(Fluid fluid) {
        this.fluidStack = fluid == Fluids.EMPTY ? FluidStack.EMPTY : new FluidStack(fluid, 1000);
        return this;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!fluidStack.isEmpty()) {
            GuiUtils.drawFluid(graphics, new Rect2i(getX(), getY(), 16, 16), fluidStack.copyWithAmount(1000), null);

            if (adjustable || showAmount) {
                Font font = Minecraft.getInstance().font;
                Component str = Component.literal(String.format("%.1fB", fluidStack.getAmount() / 1000f));
                graphics.pose().translate(0, 0, 200);
                GuiUtils.drawScaledText(graphics, font, str,
                        (int) (getX() - font.width(str) * TEXT_SCALE + 16), (int) (getY() + 16 - font.lineHeight * TEXT_SCALE),
                        0xFFFFFF, TEXT_SCALE, true);
                graphics.pose().translate(0, 0, -200);
            }

            MutableComponent c = fluidStack.copyWithAmount(1).getHoverName().copy();
            if (adjustable || showAmount) {
                c.append("\n").append(xlate("pneumaticcraft.message.misc.fluidmB", fluidStack.getAmount()).withStyle(ChatFormatting.GRAY));
            }
            c.append("\n").append(Component.literal(ModNameCache.getModName(fluidStack.getFluid()))
                    .withStyle(ChatFormatting.BLUE,  ChatFormatting.ITALIC));
            setTooltip(Tooltip.create(c));
        } else {
            setTooltip(null);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.clicked(mouseX, mouseY)) {
            if (!fluidStack.isEmpty() && adjustable) {
                boolean shift = Screen.hasShiftDown();
                switch (button) {
                    case 0 -> {  // left-click: drain 1000mB (or halve with Shift held)
                        fluidStack.setAmount(shift ? fluidStack.getAmount() / 2 : Math.max(0, fluidStack.getAmount() - 1000));
                        if (fluidStack.getAmount() < 1000) fluidStack.setAmount(0);
                    }
                    case 1 ->  // right-click: add 1000mB (or double with Shift held)
                            fluidStack.setAmount(shift ? fluidStack.getAmount() * 2 : fluidStack.getAmount() + 1000);
                    case 2 ->  // middle-click: clear slot
                            fluidStack.setAmount(0);
                }
            }
            if (pressable != null) pressable.accept(this);
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        if (this.clicked(pMouseX, pMouseY)) {
            int incr = 0;
            if (!fluidStack.isEmpty() && adjustable) {
                incr = pScrollY > 0 ? 1000 : -1000;
                if (Screen.hasShiftDown()) {
                    incr /= 10;
                }
            }
            if (incr != 0) {
                int newAmount = Math.max(0, fluidStack.getAmount() + incr);
                fluidStack.setAmount(newAmount);
                if (pressable != null) pressable.accept(this);
                return true;
            }
        }
        return super.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY);
    }

    @Override
    public void onClick(double x, double y, int button) {
        super.onClick(x, y, button);

        if (pressable != null) pressable.accept(this);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }
}
