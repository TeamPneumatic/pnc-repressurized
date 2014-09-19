package pneumaticCraft.client.render.pneumaticArmor.blockTracker;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.IBlockAccess;
import pneumaticCraft.api.client.pneumaticHelmet.IBlockTrackEntry;
import pneumaticCraft.client.gui.widget.GuiKeybindCheckBox;

public class BlockTrackEntryList{
    public List<IBlockTrackEntry> trackList = new ArrayList<IBlockTrackEntry>();

    public static BlockTrackEntryList instance = new BlockTrackEntryList();

    // initialize default Block Track Entries.
    public BlockTrackEntryList(){
        trackList.add(new BlockTrackEntryHackable());
        trackList.add(new BlockTrackEntryInventory());
        trackList.add(new BlockTrackEntryEndPortalFrame());
        trackList.add(new BlockTrackEntryMobSpawner());
        trackList.add(new BlockTrackEntrySimple());
    }

    public List<IBlockTrackEntry> getEntriesForCoordinate(IBlockAccess blockAccess, int x, int y, int z){
        List<IBlockTrackEntry> blockTrackers = new ArrayList<IBlockTrackEntry>();
        for(IBlockTrackEntry entry : trackList) {
            if(GuiKeybindCheckBox.trackedCheckboxes.get(entry.getEntryName()).checked && entry.shouldTrackWithThisEntry(blockAccess, x, y, z, blockAccess.getBlock(x, y, z))) blockTrackers.add(entry);
        }
        return blockTrackers;
    }
}
