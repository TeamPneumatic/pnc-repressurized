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

package me.desht.pneumaticcraft.common.entity.semiblock;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;

public class EntitySpawnerAgitator extends EntitySemiblockBase {
    public EntitySpawnerAgitator(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    public boolean canPlace(Direction facing) {
        return getBlockState().getBlock() == Blocks.SPAWNER;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level.isClientSide) {
            AbstractSpawner spawner = getSpawner();
            if (spawner != null) {
                // just altering the range when added isn't enough - needs to be kept updated each tick
                spawner.requiredPlayerRange = Integer.MAX_VALUE;
                if (tickCount == 1) {
                    setSpawnPersistentEntities(getSpawner(), true);
                }
            }
        }
    }

    @Override
    public void onBroken() {
        super.onBroken();

        if (!level.isClientSide) {
            AbstractSpawner spawner = getSpawner();
            if (spawner != null) {
                spawner.requiredPlayerRange = 16;
                setSpawnPersistentEntities(spawner, false);
            }
        }
    }

    private AbstractSpawner getSpawner() {
        TileEntity te = getCachedTileEntity();
        return te instanceof MobSpawnerTileEntity ? ((MobSpawnerTileEntity) te).getSpawner() : null;
    }

    private void setSpawnPersistentEntities(AbstractSpawner spawner, boolean persistent) {
        spawner.nextSpawnData.getTag().putBoolean("PersistenceRequired", persistent);
    }
}
