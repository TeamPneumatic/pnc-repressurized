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
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;

import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class BlockTrackEntryEnergy implements IBlockTrackEntry {
    public static final ResourceLocation ID = RL("block_tracker.module.energy");

    @Override
    public boolean shouldTrackWithThisEntry(Level world, BlockPos pos, BlockState state, BlockEntity te) {
        return te != null
                && !TrackerBlacklistManager.isEnergyBlacklisted(te)
                && IBlockTrackEntry.hasCapabilityOnAnyFace(te, Capabilities.EnergyStorage.BLOCK);
    }

    @Override
    public List<BlockPos> getServerUpdatePositions(BlockEntity te) {
        return te == null ? Collections.emptyList() : Collections.singletonList(te.getBlockPos());
    }

    @Override
    public int spamThreshold() {
        return 8;
    }

    @Override
    public void addInformation(Level world, BlockPos pos, BlockEntity te, Direction face, List<Component> infoList) {
        try {
            infoList.add(xlate("pneumaticcraft.blockTracker.info.rf"));
            // FIXME: getting capabilities client-side is not a reliable way to do this
            // Need a more formal framework for sync'ing server-side data to the client
            IOHelper.getEnergyStorageForBlock(te, face)
                    .ifPresent(storage -> infoList.add(Component.literal(storage.getEnergyStored() + " / " + storage.getMaxEnergyStored() + " RF")));
        } catch (Throwable e) {
            TrackerBlacklistManager.addEnergyTEToBlacklist(te, e);
        }
    }

    @Override
    public ResourceLocation getEntryID() {
        return ID;
    }
}
