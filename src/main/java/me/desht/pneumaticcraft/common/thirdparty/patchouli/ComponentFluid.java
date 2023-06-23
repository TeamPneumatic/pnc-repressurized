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

package me.desht.pneumaticcraft.common.thirdparty.patchouli;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class ComponentFluid implements ICustomComponent {
    private transient List<FluidStack> fluidStacks;
    private transient WidgetTank tankWidget;
    private transient int scaleParsed = 16000;
    public IVariable fluid;
    public IVariable scale;

    @Override
    public void build(int componentX, int componentY, int pageNum) {
        tankWidget = new WidgetTank(componentX, componentY, 16, 64, fluidStacks.isEmpty() ? FluidStack.EMPTY : fluidStacks.get(0), scaleParsed);
    }

    @Override
    public void render(PoseStack matrixStack, IComponentRenderContext ctx, float pticks, int mouseX, int mouseY) {
        if (!fluidStacks.isEmpty()) {
            tankWidget.setFluid(fluidStacks.get(ctx.getTicksInBook() / 20 % fluidStacks.size()));
        }
        if (tankWidget.getTank().getCapacity() > 0 && !tankWidget.getTank().getFluid().isEmpty()) {
            tankWidget.renderWidget(matrixStack, mouseX, mouseY, pticks);
            if (ctx.isAreaHovered(mouseX, mouseY, tankWidget.getX(), tankWidget.getY(), tankWidget.getWidth(), tankWidget.getHeight())) {
                List<Component> tooltip = new ArrayList<>();
                tankWidget.addTooltip(mouseX, mouseY, tooltip, Screen.hasShiftDown());
                ctx.setHoverTooltipComponents(tooltip);
            }
        }
    }

    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
        fluidStacks = lookup.apply(this.fluid).asStreamOrSingleton()
                .map((x) -> x.as(FluidStack.class))
                .collect(Collectors.toList());
        String scaleStr = lookup.apply(scale).asString();
        try {
            scaleParsed = Integer.parseInt(scaleStr);
        } catch (NumberFormatException e) {
            scaleParsed = 0;
        }
    }
}
