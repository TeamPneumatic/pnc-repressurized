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
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.fluid.FuelRegistry;
import me.desht.pneumaticcraft.common.inventory.ContainerLiquidCompressor;
import me.desht.pneumaticcraft.common.thirdparty.ModNameCache;
import me.desht.pneumaticcraft.common.tileentity.TileEntityLiquidCompressor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiLiquidCompressor extends GuiPneumaticContainerBase<ContainerLiquidCompressor,TileEntityLiquidCompressor> {
    public GuiLiquidCompressor(ContainerLiquidCompressor container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();
        addButton(new WidgetTank(leftPos + getFluidOffset(), topPos + 15, te.getTank()));
        WidgetAnimatedStat stat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.liquidCompressor.fuel"), new ItemStack(ModItems.LPG_BUCKET.get()), 0xFFB04000, true);
        Pair<Integer, List<ITextComponent>> p = getAllFuels();
        stat.setMinimumExpandedDimensions(p.getLeft() + 30, 17);
        stat.setText(p.getRight());
    }

    @Override
    protected void addPressureStatInfo(List<ITextComponent> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);

        pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.maxProduction",
                PneumaticCraftUtils.roundNumberTo(te.airPerTick, 2)).withStyle(TextFormatting.BLACK));
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

    private Pair<Integer,List<ITextComponent>> getAllFuels() {
        List<ITextComponent> text = new ArrayList<>();
        TranslationTextComponent header = xlate("pneumaticcraft.gui.liquidCompressor.fuelsHeader");
        text.add(header.withStyle(TextFormatting.UNDERLINE, TextFormatting.AQUA));
        int maxWidth = font.width(header);

        FuelRegistry fuelRegistry = FuelRegistry.getInstance();

        // kludge to get rid of negatively cached values (too-early init via JEI perhaps?)
        // not a big deal to clear this cache client-side since the fuel manager is only really used here on the client
        fuelRegistry.clearCachedFuelFluids();

        World world = te.getLevel();
        List<Fluid> fluids = new ArrayList<>(fuelRegistry.registeredFuels(world));
        fluids.sort((o1, o2) -> Integer.compare(fuelRegistry.getFuelValue(world, o2), fuelRegistry.getFuelValue(world, o1)));

        Map<String, Integer> counted = fluids.stream()
                .collect(Collectors.toMap(fluid -> new FluidStack(fluid, 1).getDisplayName().getString(), fluid -> 1, Integer::sum));

        int dotWidth = font.width(".");
        ITextComponent prevLine = StringTextComponent.EMPTY;
        for (Fluid fluid : fluids) {
            String value = String.format("%4d", fuelRegistry.getFuelValue(world, fluid) / 1000);
            int nSpc = (32 - font.width(value)) / dotWidth;
            value = value + StringUtils.repeat('.', nSpc);
            String fluidName = new FluidStack(fluid, 1).getDisplayName().getString();
            float mul = fuelRegistry.getBurnRateMultiplier(world, fluid);
            StringTextComponent line = mul == 1 ?
                    new StringTextComponent(value + "| " + StringUtils.abbreviate(fluidName, 25)) :
                    new StringTextComponent(value + "| " + StringUtils.abbreviate(fluidName, 20)
                            + " (x" + PneumaticCraftUtils.roundNumberTo(mul, 2) + ")");
            if (!line.equals(prevLine)) {
                maxWidth = Math.max(maxWidth, font.width(line));
                text.add(line);
            }
            prevLine = line;
            if (counted.getOrDefault(fluidName, 0) > 1) {
                ITextComponent line2 = new StringTextComponent("       " + ModNameCache.getModName(fluid)).withStyle(TextFormatting.GOLD);
                text.add(line2);
                maxWidth = Math.max(maxWidth, font.width(line2));
            }
        }

        return Pair.of(Math.min(maxWidth, leftPos - 10), text);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_LIQUID_COMPRESSOR;
    }

    @Override
    public void addProblems(List<ITextComponent> curInfo) {
        super.addProblems(curInfo);

        if (te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).isPresent()) {
            te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(fluidHandler -> {
                if (!te.isProducing && fluidHandler.getFluidInTank(0).isEmpty()) {
                    curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.liquidCompressor.noFuel"));
                }
            });
        } else {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.liquidCompressor.noFuel"));
        }
    }
}
