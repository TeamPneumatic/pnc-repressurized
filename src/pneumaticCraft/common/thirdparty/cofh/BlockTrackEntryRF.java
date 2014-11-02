package pneumaticCraft.common.thirdparty.cofh;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pneumaticCraft.api.client.pneumaticHelmet.IBlockTrackEntry;
import cofh.api.energy.IEnergyConnection;

public class BlockTrackEntryRF implements IBlockTrackEntry{

    @Override
    public boolean shouldTrackWithThisEntry(IBlockAccess world, int x, int y, int z, Block block){
        return world.getTileEntity(x, y, z) instanceof IEnergyConnection;
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
        infoList.add("blockTracker.info.rf");
    }

    @Override
    public String getEntryName(){
        return "blockTracker.module.rf";
    }

}
