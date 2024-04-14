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

package me.desht.pneumaticcraft.common.tubemodules;

import me.desht.pneumaticcraft.common.block.entity.tube.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;

public abstract class AbstractRedstoneEmittingModule extends AbstractTubeModule {
    protected int redstone;

    AbstractRedstoneEmittingModule(Direction dir, PressureTubeBlockEntity pressureTube) {
        super(dir, pressureTube);
    }

    /**
     * @param level signal level
     * @return true if the redstone has changed compared to last time.
     */
    boolean setRedstone(int level) {
        level = Mth.clamp(level, 0, 15);
        if (redstone != level) {
            redstone = level;
            updateNeighbors();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getRedstoneLevel() {
        return redstone;
    }

    @Override
    public void addInfo(List<Component> curInfo) {
        super.addInfo(curInfo);
        curInfo.add(PneumaticCraftUtils.xlate("pneumaticcraft.waila.redstoneModule.emitting", redstone));
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putInt("redstone", redstone);
        return tag;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        redstone = tag.getInt("redstone");
    }

    @Override
    public void tickCommon() {
        if (upgraded && !advancedConfig) {
            if (higherBound < lowerBound) {
                if (higherBound != lowerBound - 0.1F) {
                    higherBound = lowerBound - 0.1F;
                    sendDescriptionPacket();
                }
            } else if (higherBound != lowerBound + 0.1F) {
                higherBound = lowerBound + 0.1F;
                sendDescriptionPacket();
            }
        }
    }
}
