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
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.drone.ai.DroneAILiquidExport;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.Fluid;

import java.util.List;

public class ProgWidgetLiquidExport extends ProgWidgetInventoryBase implements ILiquidFiltered, ILiquidExport {
    public static final MapCodec<ProgWidgetLiquidExport> CODEC = RecordCodecBuilder.mapCodec(builder ->
            invParts(builder).and(
                    Codec.BOOL.optionalFieldOf("place_fluid_blocks", false).forGetter(ProgWidgetLiquidExport::isPlacingFluidBlocks)
    ).apply(builder, ProgWidgetLiquidExport::new));

    private boolean placeFluidBlocks;

    public ProgWidgetLiquidExport() {
        this(PositionFields.DEFAULT, InvBaseFields.DEFAULT, false);
    }

    private ProgWidgetLiquidExport(PositionFields pos, InvBaseFields invBaseFields, boolean placeFluidBlocks) {
        super(pos, invBaseFields);

        this.placeFluidBlocks = placeFluidBlocks;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_LIQUID_EX;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.AREA.get(), ModProgWidgetTypes.LIQUID_FILTER.get());
    }

    @Override
    public boolean isFluidValid(Fluid fluid) {
        return ProgWidgetLiquidFilter.isLiquidValid(fluid, this, 1);
    }

    @Override
    public Goal getWidgetAI(IDrone drone, IProgWidget widget) {
        return new DroneAILiquidExport(drone, (ProgWidgetInventoryBase) widget);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.ORANGE;
    }

//    @Override
//    public void writeToNBT(CompoundTag tag, HolderLookup.Provider provider) {
//        super.writeToNBT(tag, provider);
//        if (placeFluidBlocks) tag.putBoolean("placeFluidBlocks", true);
//    }
//
//    @Override
//    public void readFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
//        super.readFromNBT(tag, provider);
//        placeFluidBlocks = tag.getBoolean("placeFluidBlocks");
//    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf buf) {
        super.writeToPacket(buf);
        buf.writeBoolean(placeFluidBlocks);
    }

    @Override
    public void readFromPacket(RegistryFriendlyByteBuf buf) {
        super.readFromPacket(buf);
        placeFluidBlocks = buf.readBoolean();
    }

    @Override
    public void setPlaceFluidBlocks(boolean placeFluidBlocks) {
        this.placeFluidBlocks = placeFluidBlocks;
    }

    @Override
    public boolean isPlacingFluidBlocks() {
        return placeFluidBlocks;
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.LIQUID_EXPORT.get();
    }
}
