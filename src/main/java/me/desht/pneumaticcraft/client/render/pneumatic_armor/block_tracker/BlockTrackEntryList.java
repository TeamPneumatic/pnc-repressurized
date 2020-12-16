package me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.client.gui.widget.WidgetKeybindCheckBox;
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
    private BlockTrackEntryList() {
        trackList.add(new BlockTrackEntryHackable());
        trackList.add(new BlockTrackEntryInventory());
        trackList.add(new BlockTrackEntryFluid());
        trackList.add(new BlockTrackEntryEndPortalFrame());
        trackList.add(new BlockTrackEntryMobSpawner());
        trackList.add(new BlockTrackEntryMisc());
        trackList.add(new BlockTrackEntryEnergy());
    }

    public List<IBlockTrackEntry> getEntriesForCoordinate(IBlockReader blockAccess, BlockPos pos, TileEntity te) {
        return trackList.stream()
                .filter(entry -> WidgetKeybindCheckBox.get(entry.getEntryID()).checked && entry.shouldTrackWithThisEntry(blockAccess, pos, blockAccess.getBlockState(pos), te))
                .collect(Collectors.toList());
    }
}
