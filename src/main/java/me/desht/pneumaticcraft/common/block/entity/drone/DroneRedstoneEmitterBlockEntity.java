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

package me.desht.pneumaticcraft.common.block.entity.drone;

import me.desht.pneumaticcraft.common.block.entity.AbstractTickingBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class DroneRedstoneEmitterBlockEntity extends AbstractTickingBlockEntity {
    private IDroneBase owner;
    private final Map<Direction,Integer> signalLevels = new EnumMap<>(Direction.class);

    public DroneRedstoneEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.DRONE_REDSTONE_EMITTER.get(), pos, state);
    }

    @Override
    public boolean hasItemCapability() {
        return false;
    }

    public void setOwner(IDroneBase owner) {
        this.owner = owner;
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return null;
    }

    @Override
    public void tickServer() {
        if (owner == null || !owner.isDroneStillValid()) {
            nonNullLevel().removeBlock(getBlockPos(), false);
        } else {
            boolean changed = false;
            signalLevels.clear();
            for (Direction facing : DirectionUtil.VALUES) {
                int signal = owner.getEmittingRedstone(facing);
                if (signal != signalLevels.getOrDefault(facing, 0)) {
                    changed = true;
                }
                if (signal > 0) {
                    signalLevels.put(facing, signal);
                }
            }
            if (signalLevels.isEmpty()) {
                nonNullLevel().removeBlock(getBlockPos(), false);
            } else if (changed) {
                updateNeighbours();
            }
        }
    }

    public int getSignalLevel(Direction side) {
        return owner == null || !owner.isDroneStillValid() ? 0 : signalLevels.getOrDefault(side.getOpposite(), 0);
    }

}
