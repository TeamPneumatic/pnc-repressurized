package pneumaticCraft.client.render.pneumaticArmor.blockTracker;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pneumaticCraft.api.client.pneumaticHelmet.IBlockTrackEntry;

public class BlockTrackEntryEndPortalFrame implements IBlockTrackEntry{

    @Override
    public boolean shouldTrackWithThisEntry(IBlockAccess world, int x, int y, int z, Block block, TileEntity te){
        return block == Blocks.end_portal_frame;
    }

    @Override
    public boolean shouldBeUpdatedFromServer(TileEntity te){
        return false;
    }

    @Override
    public int spamThreshold(){
        return 10;
    }

    @Override
    public void addInformation(World world, int x, int y, int z, TileEntity te, List<String> infoList){
        if(BlockEndPortalFrame.isEnderEyeInserted(world.getBlockMetadata(x, y, z))) {
            infoList.add("Eye inserted");
        } else {
            infoList.add("Eye not inserted");
        }
    }

    @Override
    public String getEntryName(){
        return Blocks.end_portal_frame.getUnlocalizedName() + ".name";
    }

}
