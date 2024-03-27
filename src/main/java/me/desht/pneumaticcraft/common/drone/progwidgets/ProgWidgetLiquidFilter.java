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

package me.desht.pneumaticcraft.common.drone.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.registry.ModProgWidgets;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetLiquidFilter extends ProgWidget {
    private Fluid fluid = Fluids.EMPTY;

    public ProgWidgetLiquidFilter() {
        super(ModProgWidgets.LIQUID_FILTER.get());
    }

    public static ProgWidgetLiquidFilter withFilter(Fluid fluid) {
        ProgWidgetLiquidFilter f = new ProgWidgetLiquidFilter();
        f.setFluid(fluid);
        return f;
    }

    @Override
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (fluid == Fluids.EMPTY) curInfo.add(xlate("pneumaticcraft.gui.progWidget.liquidFilter.error.noLiquid"));
    }

    @Override
    public boolean hasStepInput() {
        return false;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return ModProgWidgets.LIQUID_FILTER.get();
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.LIQUID_FILTER.get());
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_LIQUID_FILTER;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        fluid = tag.contains("fluid") ?
                BuiltInRegistries.FLUID.get(new ResourceLocation(tag.getString("fluid"))) :
                Fluids.EMPTY;
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        if (fluid != Fluids.EMPTY) tag.putString("fluid", PneumaticCraftUtils.getRegistryName(fluid).orElseThrow().toString());
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf) {
        super.writeToPacket(buf);
        buf.writeFluidStack(new FluidStack(fluid, 1000));
    }

    @Override
    public void readFromPacket(FriendlyByteBuf buf) {
        super.readFromPacket(buf);
        fluid = buf.readFluidStack().getFluid();
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);
        if (fluid != null) {
            curTooltip.add(xlate("pneumaticcraft.gui.tooltip.fluid")
                    .withStyle(ChatFormatting.AQUA)
                    .append(asTextComponent()));
        }
    }

    private boolean isLiquidValid(Fluid fluid) {
        return this.fluid == null || fluid == this.fluid;
    }

    public static boolean isLiquidValid(Fluid fluid, IProgWidget mainWidget, int filterIndex) {
        // checking blacklist first
        ProgWidgetLiquidFilter widget = (ProgWidgetLiquidFilter) mainWidget.getConnectedParameters()[mainWidget.getParameters().size() + filterIndex];
        while (widget != null) {
            if (!widget.isLiquidValid(fluid)) return false;
            widget = (ProgWidgetLiquidFilter) widget.getConnectedParameters()[0];
        }
        // then whitelist
        widget = (ProgWidgetLiquidFilter) mainWidget.getConnectedParameters()[filterIndex];
        if (widget == null) return true;
        while (widget != null) {
            if (widget.isLiquidValid(fluid)) return true;
            widget = (ProgWidgetLiquidFilter) widget.getConnectedParameters()[0];
        }
        return false;
    }

    public static boolean isLiquidValid(Fluid fluid, List<ProgWidgetLiquidFilter> whitelist, List<ProgWidgetLiquidFilter> blacklist) {
        for (ProgWidgetLiquidFilter filter : blacklist) {
            if (!filter.isLiquidValid(fluid)) return false;
        }
        if (whitelist.size() == 0) return true;
        for (ProgWidgetLiquidFilter filter : whitelist) {
            if (filter.isLiquidValid(fluid)) return true;
        }
        return false;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public List<Component> getExtraStringInfo() {
        return Collections.singletonList(asTextComponent());
    }

    private Component asTextComponent() {
        return fluid != Fluids.EMPTY ?
                new FluidStack(fluid, 1).getDisplayName() :
                xlate("pneumaticcraft.gui.progWidget.liquidFilter.noFluid");
    }

    public void setFluid(Fluid fluid) {
        this.fluid = fluid;
    }

    public Fluid getFluid() {
        return fluid;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.RED;
    }
}
