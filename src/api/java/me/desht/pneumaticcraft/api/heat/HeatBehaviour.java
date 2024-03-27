/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.function.Supplier;

/**
 * This can be used to add heat dependent logic to non-BE's or blocks from outside your own mod. For example,
 * PneumaticCraft uses this to power Furnaces with heat, and to turn Lava into Obsidian when heat is drained.
 * Of course, this requires a ticking heat exchanger block (e.g. a Compressed Iron Block or any heatable machine) to
 * perform the ticking behaviour; simply adding a heat behaviour to lava won't make lava spontaneously turn into
 * obsidian. A ticking heat exchanger adjacent to the Lava block is needed to actually drain the heat.
 * <p>
 * You can extend this class, and register the extended class via
 * {@link IHeatRegistry#registerHeatBehaviour(ResourceLocation, Supplier)}
 * <p>
 * For general blockstate transitions, datapack recipes are the preferred way to add custom heat behaviours. See
 * {@code data/pneumaticcraft/recipes/block_heat_properties/*.json}
 */
public abstract class HeatBehaviour implements INBTSerializable<CompoundTag> {
    private IHeatExchangerLogic connectedHeatLogic;
    private Level world;
    private BlockPos pos;
    private BlockEntity cachedTE;
    private BlockState blockState;
    private Direction direction;  // direction of this behaviour, from the block entity's point of view

    /**
     * This method is called by the connected {@link IHeatExchangerLogic} when it initialises itself as a hull
     * heat exchanger; this happens when the owning block entity gets a neighbor block update.  You can override
     * and extend this method, but <strong>be sure to call the super method</strong>!
     * @param connectedHeatLogic the connected heat exchanger logic
     * @param world the world
     * @param pos block pos of the owning block entity
     * @param direction direction of this behaviour (from the block entity's point of view)
     */
    public HeatBehaviour initialize(IHeatExchangerLogic connectedHeatLogic, Level world, BlockPos pos, Direction direction) {
        this.connectedHeatLogic = connectedHeatLogic;
        this.world = world;
        this.pos = pos;
        this.direction = direction;
        this.cachedTE = null;
        this.blockState = null;
        return this;
    }

    public IHeatExchangerLogic getHeatExchanger() {
        return connectedHeatLogic;
    }

    public Level getWorld() {
        return world;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Direction getDirection() {
        return direction;
    }

    public BlockEntity getCachedTileEntity() {
        if (cachedTE == null || cachedTE.isRemoved())
            cachedTE = world.getBlockEntity(pos);
        return cachedTE;
    }

    public BlockState getBlockState() {
        if (blockState == null) blockState = world.getBlockState(pos);
        return blockState;
    }

    /**
     * Unique id for this behaviour, also used in NBT saving.
     *
     * @return a unique ID
     */
    public abstract ResourceLocation getId();

    /**
     * Return true when this heat behaviour is applicable for this coordinate. World access methods can be used here
     * (getWorld(), getPos(), getBlockState(), getTileEntity()).
     *
     * @return true if this behaviour is applicable here
     */
    public abstract boolean isApplicable();

    /**
     * Called every tick to update this behaviour.
     */
    public abstract void tick();

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("BlockPos", NbtUtils.writeBlockPos(pos));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        pos = NbtUtils.readBlockPos(nbt.getCompound("BlockPos"));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof HeatBehaviour behaviour) {
            return behaviour.getId().equals(getId()) && behaviour.getPos().equals(getPos());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int i = getId().hashCode();
        i = i * 31 + getPos().hashCode();
        return i;
    }
}
