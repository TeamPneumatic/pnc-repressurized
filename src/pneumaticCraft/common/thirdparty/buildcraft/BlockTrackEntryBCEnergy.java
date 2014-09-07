package pneumaticCraft.common.thirdparty.buildcraft;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pneumaticCraft.api.client.pneumaticHelmet.IBlockTrackEntry;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.power.IPowerReceptor;

/**
 * Created by Maarten on 25-Jul-14.
 */
public class BlockTrackEntryBCEnergy implements IBlockTrackEntry{
    @Override
    public boolean shouldTrackWithThisEntry(IBlockAccess world, int x, int y, int z, Block block){
        TileEntity te = world.getTileEntity(x, y, z);
        return te instanceof IPowerEmitter || te instanceof IPowerReceptor;
    }

    @Override
    public boolean shouldBeUpdatedFromServer(){
        return false;
    }

    @Override
    public int spamThreshold(){
        return 8;
    }

    @Override
    public void addInformation(World world, int x, int y, int z, List<String> infoList){
        infoList.add("blockTracker.info.bcEnergy");
    }

    @Override
    public String getEntryName(){
        return "blockTracker.module.bcEnergy";
    }
}
