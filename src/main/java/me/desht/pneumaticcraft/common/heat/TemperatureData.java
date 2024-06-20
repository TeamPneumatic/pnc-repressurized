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
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

public class TemperatureData {
    private final Double[] temp;// = new Double[7];  // 6 faces plus null "face"
    private final boolean isMultisided;

    public static TemperatureData fromNBT(CompoundTag nbt) {
        Double[] temp = new Double[7];
        if (nbt.contains("heat")) {
            ListTag tagList = nbt.getList("heat", Tag.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                CompoundTag heatTag = tagList.getCompound(i);
                temp[heatTag.getByte("side")] = (double) heatTag.getInt("temp");
            }
            return new TemperatureData(true, temp);
        } else {
            temp[6] = (double) nbt.getInt("temp");
            return new TemperatureData(false, temp);
        }
    }

    private TemperatureData(boolean isMultisided, Double[] temp) {
        this.isMultisided = isMultisided;
        this.temp = temp;
    }

    public static TemperatureData forBlockEntity(BlockEntity provider) {
        Set<IHeatExchangerLogic> heatExchangers = new HashSet<>();
        for (Direction face : DirectionUtil.VALUES) {
            IOHelper.getCap(provider, PNCCapabilities.HEAT_EXCHANGER_BLOCK, face).ifPresent(heatExchangers::add);
        }

        Double[] temp = new Double[7];

        if (heatExchangers.size() > 1) {
            for (Direction face : DirectionUtil.VALUES) {
                IOHelper.getCap(provider, PNCCapabilities.HEAT_EXCHANGER_BLOCK, face)
                        .ifPresent(h -> temp[face.get3DDataValue()] = h.getTemperature());
            }
            return new TemperatureData(true, temp);
        }

        IOHelper.getCap(provider, PNCCapabilities.HEAT_EXCHANGER_BLOCK, null)
                .ifPresent(h -> temp[6] = h.getTemperature());
        return new TemperatureData(false, temp);
    }

    public boolean isMultisided() {
        return isMultisided;
    }

    public Double getTemperature(Direction face) {
        return temp[face.get3DDataValue()];
    }

    public int getTemperatureAsInt(Direction face) {
        return hasData(face) ? temp[face.get3DDataValue()].intValue() : 0;
    }

    public boolean hasData(Direction face) {
        return face == null ? temp[6] != null : temp[face.get3DDataValue()] != null;
    }

    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        if (isMultisided()) {
            ListTag tagList = new ListTag();
            for (Direction face : DirectionUtil.VALUES) {
                if (hasData(face)) {
                    int temp = getTemperatureAsInt(face);
                    tagList.add(Util.make(new CompoundTag(), t -> {
                        t.putByte("side", (byte) face.get3DDataValue());
                        t.putInt("temp", temp);
                    }));
                }
            }
            nbt.put("heat", tagList);
        } else {
            nbt.putInt("temp", getTemperatureAsInt(null));
        }
        return nbt;
    }
}
