package pneumaticCraft.client.render.pneumaticArmor.blockTracker;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import pneumaticCraft.api.client.pneumaticHelmet.IBlockTrackEntry;

public class BlockTrackEntrySimple implements IBlockTrackEntry{

    @Override
    public boolean shouldTrackWithThisEntry(World world, int x, int y, int z, Block block){
        return block == Blocks.tnt || block == Blocks.tripwire_hook || block == Blocks.monster_egg;
    }

    @Override
    public boolean shouldBeUpdatedFromServer(){
        return false;
    }

    @Override
    public int spamThreshold(){
        return 10;
    }

    @Override
    public void addInformation(World world, int x, int y, int z, List<String> infoList){}

    @Override
    public String getEntryName(){
        return "blockTracker.module.misc";
    }
}
