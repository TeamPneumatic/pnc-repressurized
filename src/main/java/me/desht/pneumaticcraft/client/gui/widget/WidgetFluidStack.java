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

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.common.thirdparty.ModNameCache;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Consumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class WidgetFluidStack extends WidgetFluidFilter {
    private static final float TEXT_SCALE = 0.5f;

    private boolean adjustable = false;

    public WidgetFluidStack(int x, int y, FluidStack stack, Consumer<WidgetFluidFilter> pressable) {
        super(x, y, stack, pressable);
    }

    public WidgetFluidStack setAdjustable() {
        this.adjustable = true;
        return this;
    }

    public FluidStack getFluidStack() {
        return fluidStack;
    }

    public WidgetFluidStack setFluidStack(FluidStack fluidStack) {
        this.fluidStack = fluidStack;
        return this;
    }

    @Override
    public void renderWidget(PoseStack matrixStack, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(matrixStack, mouseX, mouseY, partialTick);

        if (!fluidStack.isEmpty()) {
            int fluidAmount = fluidStack.getAmount() / 1000;
            if (fluidAmount > 1 || adjustable) {
                Font font = Minecraft.getInstance().font;
                String str = fluidAmount + "B";
                matrixStack.pushPose();
                matrixStack.translate(getX() - font.width(str) * TEXT_SCALE + 16, getY() + 16 - font.lineHeight * TEXT_SCALE, 200);
                matrixStack.scale(TEXT_SCALE, TEXT_SCALE, TEXT_SCALE);
                font.drawShadow(matrixStack, str, 0, 0, 0xFFFFFFFF);
                matrixStack.popPose();
            }
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
            return false;
        }
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<Component> curTip, boolean shiftPressed) {
        if (!fluidStack.isEmpty()) {
            curTip.add(new FluidStack(fluidStack, 1).getDisplayName());
            if (adjustable) {
                curTip.add(xlate("pneumaticcraft.message.misc.fluidmB", fluidStack.getAmount()).withStyle(ChatFormatting.GRAY));
            }
            curTip.add(Component.literal(ModNameCache.getModName(fluidStack.getFluid()))
                    .withStyle(ChatFormatting.BLUE,  ChatFormatting.ITALIC));
        }
    }
}
