package me.desht.pneumaticcraft.client.render.pneumaticArmor.blockTracker;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IBlockTrackEntry;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class BlockTrackEntryEndPortalFrame implements IBlockTrackEntry {

    @Override
    public boolean shouldTrackWithThisEntry(IBlockAccess world, BlockPos pos, IBlockState state, TileEntity te) {
        return state.getBlock() == Blocks.END_PORTAL_FRAME;
    }

    @Override
    public boolean shouldBeUpdatedFromServer(TileEntity te) {
        return false;
    }

    @Override
    public int spamThreshold() {
        return 10;
    }

    @Override
    public void addInformation(World world, BlockPos pos, TileEntity te, List<String> infoList) {
        if (world.getBlockState(pos).getValue(BlockEndPortalFrame.EYE)) {
            infoList.add("Eye inserted");
        } else {
            infoList.add("Eye not inserted");
        }
    }

    @Override
    public String getEntryName() {
        return Blocks.END_PORTAL_FRAME.getUnlocalizedName() + ".name";
    }

}
