package pneumaticCraft.common.thirdparty.hydraulicraft;

import java.util.List;

import k4unl.minecraft.Hydraulicraft.api.IHydraulicMachine;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pneumaticCraft.api.client.pneumaticHelmet.IBlockTrackEntry;

/**
 * Created by Maarten on 25-Jul-14.
 */
public class BlockTrackEntryHydraulicraft implements IBlockTrackEntry{
    @Override
    public boolean shouldTrackWithThisEntry(IBlockAccess world, int x, int y, int z, Block block){
        return world.getTileEntity(x, y, z) instanceof IHydraulicMachine;
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
        infoList.add("blockTracker.info.hydraulicraft");
    }

    @Override
    public String getEntryName(){
        return "blockTracker.module.hydraulicraft";
    }
}
