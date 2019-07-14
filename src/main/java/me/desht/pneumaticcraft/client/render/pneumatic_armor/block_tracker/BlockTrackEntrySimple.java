package me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SilverfishBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;

public class BlockTrackEntrySimple implements IBlockTrackEntry {

    @Override
    public boolean shouldTrackWithThisEntry(IBlockReader world, BlockPos pos, BlockState state, TileEntity te) {
        Block block = state.getBlock();
        return block == Blocks.TNT || block == Blocks.TRIPWIRE || block instanceof SilverfishBlock;
    }

    @Override
    public boolean getServerUpdatePositions(TileEntity te) {
        return false;
    }

    @Override
    public int spamThreshold() {
        return 10;
    }

    @Override
    public void addInformation(World world, BlockPos pos, TileEntity te, Direction face, List<String> infoList) {
    }

    @Override
    public String getEntryName() {
        return "blockTracker.module.misc";
    }
}
