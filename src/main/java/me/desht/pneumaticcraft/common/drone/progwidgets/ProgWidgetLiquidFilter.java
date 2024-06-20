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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetLiquidFilter extends ProgWidget {
    public static final MapCodec<ProgWidgetLiquidFilter> CODEC = RecordCodecBuilder.mapCodec(builder ->
        baseParts(builder).and(
                FluidStack.OPTIONAL_CODEC.optionalFieldOf("fluid", FluidStack.EMPTY).forGetter(ProgWidgetLiquidFilter::getFluidStack)
    ).apply(builder, ProgWidgetLiquidFilter::new));

    private FluidStack fluidStack;

    public ProgWidgetLiquidFilter(PositionFields pos, FluidStack fluidStack) {
        super(pos);

        this.fluidStack = fluidStack;
    }

    public ProgWidgetLiquidFilter() {
        this(PositionFields.DEFAULT, FluidStack.EMPTY);
    }

    public static ProgWidgetLiquidFilter withFilter(Fluid fluid) {
        return withFilter(new FluidStack(fluid, 1000));
    }

    public static ProgWidgetLiquidFilter withFilter(FluidStack fluid) {
        ProgWidgetLiquidFilter f = new ProgWidgetLiquidFilter();
        f.setFluidStack(fluid);
        return f;
    }

    @Override
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);

        if (fluidStack.isEmpty()) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.liquidFilter.error.noLiquid"));
        }
    }

    @Override
    public boolean hasStepInput() {
        return false;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return ModProgWidgetTypes.LIQUID_FILTER.get();
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.LIQUID_FILTER.get());
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_LIQUID_FILTER;
    }

//    @Override
//    public void readFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
//        super.readFromNBT(tag, provider);
//        fluidStack = tag.contains("fluid") ?
//                BuiltInRegistries.FLUID.get(new ResourceLocation(tag.getString("fluid"))) :
//                Fluids.EMPTY;
//    }
//
//    @Override
//    public void writeToNBT(CompoundTag tag, HolderLookup.Provider provider) {
//        super.writeToNBT(tag, provider);
//        if (fluidStack != Fluids.EMPTY) tag.putString("fluid", PneumaticCraftUtils.getRegistryName(fluidStack).orElseThrow().toString());
//    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf buf) {
        super.writeToPacket(buf);
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, fluidStack);
    }

    @Override
    public void readFromPacket(RegistryFriendlyByteBuf buf) {
        super.readFromPacket(buf);
        fluidStack = FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.LIQUID_FILTER.get();
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);
        if (fluidStack != null) {
            curTooltip.add(xlate("pneumaticcraft.gui.tooltip.fluid")
                    .withStyle(ChatFormatting.AQUA)
                    .append(asTextComponent()));
        }
    }

    private boolean isLiquidValid(Fluid fluid) {
        return this.fluidStack.isEmpty() || fluid == this.fluidStack.getFluid();
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
        return whitelist.isEmpty() || whitelist.stream().anyMatch(filter -> filter.isLiquidValid(fluid));
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
        return !fluidStack.isEmpty() ?
                fluidStack.getHoverName() :
                xlate("pneumaticcraft.gui.progWidget.liquidFilter.noFluid");
    }

    public void setFluidStack(FluidStack fluidStack) {
        this.fluidStack = fluidStack;
    }

    public FluidStack getFluidStack() {
        return fluidStack;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.RED;
    }
}
