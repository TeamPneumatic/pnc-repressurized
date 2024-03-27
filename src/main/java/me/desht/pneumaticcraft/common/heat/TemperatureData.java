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

package me.desht.pneumaticcraft.common.heat;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TemperatureData implements INBTSerializable<CompoundTag> {
    private final Double[] temp = new Double[7];  // 6 faces plus null "face"

    private boolean isMultisided;

    public static TemperatureData fromNBT(CompoundTag nbt) {
        TemperatureData data = new TemperatureData();
        data.deserializeNBT(nbt);
        return data;
    }

    private TemperatureData() {
        isMultisided = false;
    }

    public TemperatureData(BlockEntity provider) {
        Arrays.fill(temp, null);

        Set<IHeatExchangerLogic> heatExchangers = new HashSet<>();
        for (Direction face : DirectionUtil.VALUES) {
            IOHelper.getCap(provider, PNCCapabilities.HEAT_EXCHANGER_BLOCK, face).ifPresent(heatExchangers::add);
        }

        if (heatExchangers.size() > 1) {
            isMultisided = true;
            for (Direction face : DirectionUtil.VALUES) {
                IOHelper.getCap(provider, PNCCapabilities.HEAT_EXCHANGER_BLOCK, face)
                        .ifPresent(h -> temp[face.get3DDataValue()] = h.getTemperature());
            }
        } else if (heatExchangers.size() == 1) {
            isMultisided = false;
            IOHelper.getCap(provider, PNCCapabilities.HEAT_EXCHANGER_BLOCK, null)
                    .ifPresent(h -> temp[6] = h.getTemperature());
        } else {
            isMultisided = false;
        }
    }

    public boolean isMultisided() {
        return isMultisided;
    }

    public double getTemperature(Direction face) {
        return face == null ? temp[6] : temp[face.get3DDataValue()];
    }

    public boolean hasData(Direction face) {
        return face == null ? temp[6] != null : temp[face.get3DDataValue()] != null;
    }

    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        if (isMultisided()) {
            ListTag tagList = new ListTag();
            for (Direction face : DirectionUtil.VALUES) {
                if (temp[face.get3DDataValue()] != null) {
                    CompoundTag heatTag = new CompoundTag();
                    heatTag.putByte("side", (byte) face.get3DDataValue());
                    heatTag.putInt("temp", (int) getTemperature(face));
                    tagList.add(heatTag);
                }
            }
            nbt.put("heat", tagList);
        } else {
            nbt.putInt("temp", (int) getTemperature(null));
        }
        return nbt;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (isMultisided()) {
            ListTag tagList = new ListTag();
            for (Direction face : DirectionUtil.VALUES) {
                CompoundTag heatTag = new CompoundTag();
                heatTag.putByte("side", (byte) face.get3DDataValue());
                heatTag.putInt("temp", (int) getTemperature(face));
                tagList.add(heatTag);
            }
            nbt.put("heat", tagList);
        } else {
            nbt.putInt("temp", (int) getTemperature(null));
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("heat")) {
            isMultisided = true;
            ListTag tagList = nbt.getList("heat", Tag.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                CompoundTag heatTag = tagList.getCompound(i);
                temp[heatTag.getByte("side")] = (double) heatTag.getInt("temp");
            }
        } else {
            isMultisided = false;
            temp[6] = (double) nbt.getInt("temp");
        }
    }
}
