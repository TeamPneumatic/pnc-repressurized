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
import me.desht.pneumaticcraft.common.drone.ai.DroneAIBlockCondition;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIDig;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ProgWidgetBlockCondition extends ProgWidgetCondition {
    public static final MapCodec<ProgWidgetBlockCondition> CODEC = RecordCodecBuilder.mapCodec(builder ->
            condParts(builder).and(builder.group(
                            Codec.BOOL.optionalFieldOf("check_air", false).forGetter(p -> p.checkingForAir),
                            Codec.BOOL.optionalFieldOf("check_liquid", false).forGetter(p -> p.checkingForLiquids)
                    )
            ).apply(builder, ProgWidgetBlockCondition::new));

    public boolean checkingForAir;
    public boolean checkingForLiquids;

    public ProgWidgetBlockCondition() {
    }

    public ProgWidgetBlockCondition(PositionFields pos, InvBaseFields inv, ConditionFields cond, boolean checkingForAir, boolean checkingForLiquids) {
        super(pos, inv, cond);

        this.checkingForAir = checkingForAir;
        this.checkingForLiquids = checkingForLiquids;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.AREA.get(), ModProgWidgetTypes.ITEM_FILTER.get(), ModProgWidgetTypes.TEXT.get());
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(IDrone drone, IProgWidget widget) {
        return new DroneAIBlockCondition(drone, (ProgWidgetAreaItemBase) widget) {
            @Override
            protected boolean evaluate(BlockPos pos) {
                boolean ret = false;
                if (checkingForAir && drone.getDroneLevel().isEmptyBlock(pos)) {
                    ret = true;
                } else if (checkingForLiquids && PneumaticCraftUtils.isBlockLiquid(drone.getDroneLevel().getBlockState(pos).getBlock())) {
                    ret = true;
                } else if (!checkingForAir && !checkingForLiquids || getConnectedParameters()[1] != null) {
                    ret = DroneAIDig.isBlockValidForFilter(drone.getDroneLevel(), pos, drone, progWidget);
                }
                maybeRecordMeasuredVal(drone, ret ? 1 : 0);
                return ret;
            }
        };
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_BLOCK;
    }

//    @Override
//    public void writeToNBT(CompoundTag tag, HolderLookup.Provider provider) {
//        super.writeToNBT(tag, provider);
//        if (checkingForAir) tag.putBoolean("checkingForAir", true);
//        if (checkingForLiquids) tag.putBoolean("checkingForLiquids", true);
//    }
//
//    @Override
//    public void readFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
//        super.readFromNBT(tag, provider);
//        checkingForAir = tag.getBoolean("checkingForAir");
//        checkingForLiquids = tag.getBoolean("checkingForLiquids");
//    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.CONDITION_BLOCK.get();
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf buf) {
        super.writeToPacket(buf);
        buf.writeBoolean(checkingForAir);
        buf.writeBoolean(checkingForLiquids);
    }

    @Override
    public void readFromPacket(RegistryFriendlyByteBuf buf) {
        super.readFromPacket(buf);
        checkingForAir = buf.readBoolean();
        checkingForLiquids = buf.readBoolean();
    }
}
