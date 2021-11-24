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

package me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.client.gui.widget.WidgetKeybindCheckBox;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import java.util.List;
import java.util.stream.Collectors;

public enum BlockTrackEntryList {
    INSTANCE;

    public final NonNullList<IBlockTrackEntry> trackList = NonNullList.create();

    // initialize default Block Track Entries.
    BlockTrackEntryList() {
        trackList.add(new BlockTrackEntryHackable());
        trackList.add(new BlockTrackEntryInventory());
        trackList.add(new BlockTrackEntryFluid());
        trackList.add(new BlockTrackEntryEndPortalFrame());
        trackList.add(new BlockTrackEntryMobSpawner());
        trackList.add(new BlockTrackEntryMisc());
        trackList.add(new BlockTrackEntryEnergy());
    }

    public List<IBlockTrackEntry> getEntriesForCoordinate(IBlockReader blockAccess, BlockPos pos, TileEntity te) {
        final BlockState state = blockAccess.getBlockState(pos);
        return trackList.stream()
                .filter(entry -> WidgetKeybindCheckBox.get(entry.getEntryID()).checked
                        && entry.shouldTrackWithThisEntry(blockAccess, pos, state, te))
                .collect(Collectors.toList());
    }
}
