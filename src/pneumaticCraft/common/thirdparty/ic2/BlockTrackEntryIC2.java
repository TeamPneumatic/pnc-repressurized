package pneumaticCraft.common.thirdparty.ic2;

import ic2.api.energy.tile.*;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import pneumaticCraft.api.client.pneumaticHelmet.IBlockTrackEntry;

import java.util.List;

/**
 * Created by Maarten on 25-Jul-14.
 */
public class BlockTrackEntryIC2 implements IBlockTrackEntry {
    @Override
    public boolean shouldTrackWithThisEntry(World world, int x, int y, int z, Block block) {
        return world.getTileEntity(x,y,z) instanceof IEnergyTile;
    }

    @Override
    public boolean shouldBeUpdatedFromServer() {
        return false;
    }

    @Override
    public int spamThreshold() {
        return 8;
    }

    @Override
    public void addInformation(World world, int x, int y, int z, List<String> infoList) {
        infoList.add("blockTracker.info.ic2");
    }

    @Override
    public String getEntryName() {
        return "blockTracker.module.ic2";
    }
}
