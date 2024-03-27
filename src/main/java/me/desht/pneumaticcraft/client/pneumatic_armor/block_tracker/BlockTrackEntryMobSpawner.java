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

package me.desht.pneumaticcraft.client.pneumatic_armor.block_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.common.entity.semiblock.SpawnerAgitatorEntity;
import me.desht.pneumaticcraft.common.hacking.block.HackableMobSpawner;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.mixin.accessors.BaseSpawnerAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class BlockTrackEntryMobSpawner implements IBlockTrackEntry {
    public static final ResourceLocation ID = RL("block_tracker.module.spawner");

    @Override
    public boolean shouldTrackWithThisEntry(BlockGetter world, BlockPos pos, BlockState state, BlockEntity te) {
        return state.getBlock() == Blocks.SPAWNER;
    }

    @Override
    public List<BlockPos> getServerUpdatePositions(BlockEntity te) {
        return te == null ? Collections.emptyList() : Collections.singletonList(te.getBlockPos());
    }

    @Override
    public int spamThreshold() {
        return 10;
    }

    @Override
    public void addInformation(Level world, BlockPos pos, BlockEntity te, Direction face, List<Component> infoList) {
        // FIXME translations
        if (te instanceof SpawnerBlockEntity spawnerBlockEntity) {
            BaseSpawner spawner = spawnerBlockEntity.getSpawner();
            Entity e = spawner.getOrCreateDisplayEntity(world, spawnerBlockEntity.getBlockPos());
            if (e == null) {
                // seems to happen with enderman spawners, possibly related to EndermanEntity#readAdditional() doing a bad world cast
                // certainly spams a lot a vanilla-related errors
                infoList.add(Component.literal("<ERROR> Missing entity?"));
                return;
            }
            infoList.add(xlate("pneumaticcraft.blockTracker.info.spawner.type", e.getName().getString()));
            if (isNearPlayer(spawner, world, pos) || hasAgitator(world, pos)) {
                infoList.add(xlate("pneumaticcraft.blockTracker.info.spawner.time",
                        PneumaticCraftUtils.convertTicksToMinutesAndSeconds(((BaseSpawnerAccess)spawner).getSpawnDelay(), false)));
            } else if (HackableMobSpawner.isHacked(world, pos)) {
                infoList.add(xlate("pneumaticcraft.blockTracker.info.spawner.hacked"));
            } else {
                infoList.add(xlate("pneumaticcraft.blockTracker.info.spawner.standby"));
            }
        }
    }

    private boolean isNearPlayer(BaseSpawner spawner, Level pLevel, BlockPos pPos) {
        return pLevel.hasNearbyAlivePlayer(pPos.getX() + 0.5D, pPos.getY() + 0.5D, pPos.getZ() + 0.5D, ((BaseSpawnerAccess)spawner).getRequiredPlayerRange());
    }

    private boolean hasAgitator(Level world, BlockPos pos) {
        return SemiblockTracker.getInstance().getSemiblock(world, pos) instanceof SpawnerAgitatorEntity;
    }

    @Override
    public ResourceLocation getEntryID() {
        return ID;
    }
}
