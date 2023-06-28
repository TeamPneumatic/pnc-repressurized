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

package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.FluidMixerBlockEntity;
import me.desht.pneumaticcraft.common.core.ModRecipeTypes;
import me.desht.pneumaticcraft.common.inventory.FluidMixerMenu;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class FluidMixerScreen extends AbstractPneumaticCraftContainerScreen<FluidMixerMenu, FluidMixerBlockEntity> {
    private final WidgetButtonExtended[] dumpButtons = new WidgetButtonExtended[2];

    public FluidMixerScreen(FluidMixerMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
        imageHeight = 212;
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(new WidgetTank(leftPos + 13, topPos + 19, te.getInputTank1()));
        addRenderableWidget(new WidgetTank(leftPos + 33, topPos + 19, te.getInputTank2()));
        addRenderableWidget(new WidgetTank(leftPos + 99, topPos + 19, te.getOutputTank()));

        for (int i = 0; i < 2; i++) {
            dumpButtons[i] = new WidgetButtonExtended(leftPos + 14 + i * 20, topPos + 86, 14, 14, Component.empty())
                    .withTag("dump" + (i + 1));
            addRenderableWidget(dumpButtons[i]);
        }
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_FLUID_MIXER;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
        super.renderBg(graphics, partialTicks, x, y);

        // animated progress bar
        int progressWidth = (int) (te.getCraftingPercentage() * 48);
        graphics.blit(getGuiTexture(), leftPos + 50, topPos + 36, imageWidth, 0, progressWidth, 30);
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - imageWidth) / 2;
        int yStart = (height - imageHeight) / 2;
        return new PointXY(xStart + imageWidth * 3 / 4 + 14, yStart + imageHeight / 4 - 2);
    }

    @Override
    public void containerTick() {
        super.containerTick();

        for (int i = 0; i < 2; i++) {
            String k = hasShiftDown() ? "dumpInput" : "moveInput";
            dumpButtons[i].setMessage(hasShiftDown() ? Component.literal("X").withStyle(ChatFormatting.RED) : Component.literal(Symbols.TRIANGLE_RIGHT).withStyle(ChatFormatting.DARK_AQUA));
            dumpButtons[i].setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.thermopneumatic." + k)));
        }
    }

    @Override
    protected void addPressureStatInfo(List<Component> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);

        if (te.didWork) {
            pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.airUsage", PneumaticCraftUtils.roundNumberTo(2.5f * te.getPressure(), 2)));
        }
    }

    @Override
    protected void addProblems(List<Component> curInfo) {
        super.addProblems(curInfo);

        if (te.maxProgress == 0) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.missingIngredients"));
        }
    }

    @Override
    public Collection<ItemStack> getTargetItems() {
        return getCurrentRecipe(ModRecipeTypes.FLUID_MIXER.get())
                .map(recipe -> Collections.singletonList(recipe.getOutputItem()))
                .orElse(Collections.emptyList());
    }

    @Override
    public Collection<FluidStack> getTargetFluids() {
        return getCurrentRecipe(ModRecipeTypes.FLUID_MIXER.get())
                .map(recipe -> Collections.singletonList(recipe.getOutputFluid()))
                .orElse(Collections.emptyList());
    }
}
