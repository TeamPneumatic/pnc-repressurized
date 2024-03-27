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

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class HeatExtractionTracker extends SavedData {
    private static final String DATA_NAME = "PneumaticCraftHeatExtraction";

    private final Map<BlockPos, Double> extracted = new HashMap<>();

    public static SavedData.Factory<HeatExtractionTracker> factory() {
        return new SavedData.Factory<>(HeatExtractionTracker::new, HeatExtractionTracker::load, null);
    }

    private HeatExtractionTracker() {
    }

    private static HeatExtractionTracker load(CompoundTag tag) {
        return new HeatExtractionTracker().readNBT(tag);
    }

    public static HeatExtractionTracker getInstance(Level world) {
        return ((ServerLevel) world).getDataStorage().computeIfAbsent(HeatExtractionTracker.factory(), DATA_NAME);
    }

    public double getHeatExtracted(BlockPos pos) {
        return extracted.getOrDefault(pos, 0.0);
    }

    public void extractHeat(BlockPos pos, double heat) {
        double newAmount = getHeatExtracted(pos) + heat;
        if (Math.abs(newAmount) < 0.000001) {
            extracted.remove(pos);
        } else {
            extracted.put(pos, newAmount);
        }
        setDirty();
    }

    private HeatExtractionTracker readNBT(CompoundTag nbt) {
        extracted.clear();

        ListTag list = nbt.getList("extracted", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag sub = list.getCompound(i);
            BlockPos pos = new BlockPos(sub.getInt("x"), sub.getInt("y"), sub.getInt("z"));
            extracted.put(pos, sub.getDouble("heat"));
        }

        return this;
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        ListTag list = new ListTag();
        extracted.forEach((pos, heat) -> {
            CompoundTag sub = new CompoundTag();
            sub.putInt("x", pos.getX());
            sub.putInt("y", pos.getY());
            sub.putInt("z", pos.getZ());
            sub.putDouble("heat", heat);
            list.add(sub);
        });
        compound.put("extracted", list);
        return compound;
    }
}
