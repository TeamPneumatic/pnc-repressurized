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

import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

// Note: spawner agitator logic is handled by BaseSpawnerMixin for player-near checking,
// and in MiscEventHandler#onMobSpawn() for tagging & mob persistence

public class SpawnerAgitatorEntity extends AbstractSemiblockEntity {
    public SpawnerAgitatorEntity(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    public boolean canPlace(Direction facing) {
        return getBlockState().getBlock() == Blocks.SPAWNER;
    }

    public static boolean isAgitated(BaseSpawner spawner) {
        return Objects.requireNonNull(spawner.getOwner()).left()
                .map(be -> SemiblockTracker.getInstance().getSemiblock(be.getLevel(), be.getBlockPos()) instanceof SpawnerAgitatorEntity)
                .orElse(false);
    }
}
