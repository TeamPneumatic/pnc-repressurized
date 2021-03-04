package me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SilverfishBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class BlockTrackEntryMisc implements IBlockTrackEntry {
    private static final ResourceLocation ID = RL("block_tracker.module.misc");

    @Override
    public boolean shouldTrackWithThisEntry(IBlockReader world, BlockPos pos, BlockState state, TileEntity te) {
        Block block = state.getBlock();
        return block == Blocks.TNT || block == Blocks.TRIPWIRE || block instanceof SilverfishBlock || block == Blocks.BEE_NEST;
    }

    @Override
    public List<BlockPos> getServerUpdatePositions(TileEntity te) {
        return Collections.emptyList();
    }

    @Override
    public int spamThreshold() {
        return 10;
    }

    @Override
    public void addInformation(World world, BlockPos pos, TileEntity te, Direction face, List<ITextComponent> infoList) {
    }

    @Override
    public ResourceLocation getEntryID() {
        return ID;
    }
}
