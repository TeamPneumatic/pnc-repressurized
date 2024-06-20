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

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetKeybindCheckBox;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public enum BlockTrackHandler {
    INSTANCE;

    private final Map<ResourceLocation, Supplier<? extends IBlockTrackEntry>> pendingTrackers = new ConcurrentHashMap<>();
    private List<IBlockTrackEntry> trackers;

    public static BlockTrackHandler getInstance() {
        return INSTANCE;
    }

    public void registerDefaultEntries() {
        register(BlockTrackEntryHackable.ID, BlockTrackEntryHackable::new);
        register(BlockTrackEntryInventory.ID, BlockTrackEntryInventory::new);
        register(BlockTrackEntryFluid.ID, BlockTrackEntryFluid::new);
        register(BlockTrackEntryEndPortalFrame.ID, BlockTrackEntryEndPortalFrame::new);
        register(BlockTrackEntryMobSpawner.ID, BlockTrackEntryMobSpawner::new);
        register(BlockTrackEntryMisc.ID, BlockTrackEntryMisc::new);
        register(BlockTrackEntryEnergy.ID, BlockTrackEntryEnergy::new);
    }

    public void register(ResourceLocation id, @Nonnull Supplier<? extends IBlockTrackEntry> entry) {
        if (trackers != null) throw new IllegalStateException("entity tracker registry is frozen!");
        pendingTrackers.put(id, entry);
    }

    public void freeze() {
        ImmutableList.Builder<IBlockTrackEntry> builder = ImmutableList.builder();
        for (var sup : pendingTrackers.values()) {
            builder.add(sup.get());
        }
        trackers = builder.build();
    }

    public List<IBlockTrackEntry> getEntriesForCoordinate(Level blockAccess, BlockPos pos, BlockEntity te) {
        final BlockState state = blockAccess.getBlockState(pos);
        List<IBlockTrackEntry> trackers = new ArrayList<>();
        for (IBlockTrackEntry tracker : this.trackers) {
            WidgetCheckBox checkBox = WidgetKeybindCheckBox.get(tracker.getEntryID());
            if (checkBox != null && checkBox.isChecked() && tracker.shouldTrackWithThisEntry(blockAccess, pos, state, te)) {
                trackers.add(tracker);
            }
        }
        return trackers;
    }

    public Collection<ResourceLocation> getIDs() {
        return ImmutableList.copyOf(pendingTrackers.keySet());
    }

    public List<IBlockTrackEntry> getEntries() {
        return trackers;
    }
}
