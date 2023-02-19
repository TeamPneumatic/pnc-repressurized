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

import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.LiquidCompressorBlockEntity;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.fluid.FuelRegistry;
import me.desht.pneumaticcraft.common.inventory.LiquidCompressorMenu;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class LiquidCompressorScreen extends AbstractPneumaticCraftContainerScreen<LiquidCompressorMenu,LiquidCompressorBlockEntity> {
    public LiquidCompressorScreen(LiquidCompressorMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();
        addRenderableWidget(new WidgetTank(leftPos + getFluidOffset(), topPos + 15, te.getTank()));
        WidgetAnimatedStat stat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.liquidCompressor.fuel"), new ItemStack(ModItems.LPG_BUCKET.get()), 0xFFB04000, true);
        Pair<Integer, List<Component>> p = ClientUtils.formatFuelList(xlate("pneumaticcraft.gui.liquidCompressor.fuelsHeader"),
                getGuiLeft() - 10,
                fluid -> FuelRegistry.getInstance().getFuelValue(ClientUtils.getClientLevel(), fluid) / 1000,
                true
        );
        stat.setMinimumExpandedDimensions(p.getLeft() + 30, 17);
        stat.setText(p.getRight());
    }

    @Override
    protected void addPressureStatInfo(List<Component> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);

        pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.maxProduction",
                PneumaticCraftUtils.roundNumberTo(te.airPerTick, 2)).withStyle(ChatFormatting.BLACK));
    }

    protected int getFluidOffset() {
        return 86;
    }

    @Override
    protected PointXY getInvNameOffset() {
        return new PointXY(0, -1);
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - imageWidth) / 2;
        int yStart = (height - imageHeight) / 2;
        return new PointXY(xStart + imageWidth * 3 / 4 + 5, yStart + imageHeight / 4 + 4);
    }

    @Override
    protected String upgradeCategory() {
        return "liquid_compressor";
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_LIQUID_COMPRESSOR;
    }

    @Override
    public void addProblems(List<Component> curInfo) {
        super.addProblems(curInfo);

        if (te.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent()) {
            te.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(fluidHandler -> {
                if (!te.isActive() && fluidHandler.getFluidInTank(0).isEmpty()) {
                    curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.liquidCompressor.noFuel"));
                }
            });
        } else {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.liquidCompressor.noFuel"));
        }
    }
}
