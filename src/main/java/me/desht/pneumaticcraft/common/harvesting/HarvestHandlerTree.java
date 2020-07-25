package me.desht.pneumaticcraft.common.harvesting;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.harvesting.HarvestHandler;
import me.desht.pneumaticcraft.common.core.ModHarvestHandlers.TreePart;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;

public class HarvestHandlerTree extends HarvestHandler {
    private static final int SAPLING_PICK_RANGE = 8;

    @Override
    public boolean canHarvest(World world, IBlockReader chunkCache, BlockPos pos, BlockState state, IDrone drone) {
        return state.getBlock().isIn(BlockTags.LOGS);
    }

    @Override
    public boolean harvestAndReplant(World world, IBlockReader chunkCache, BlockPos pos, BlockState state, IDrone drone) {
        harvest(world, chunkCache, pos, state, drone);

        // this will work for all vanilla trees, and any modded trees where the mod is consistent about log/sapling naming
        Block saplingBlock = TreePart.LOG.convert(state.getBlock(), TreePart.SAPLING);

        if (saplingBlock != null && saplingBlock != Blocks.AIR) {
            BlockState saplingState = saplingBlock.getDefaultState();
            if (saplingState.isValidPosition(world, pos)) {
                List<ItemEntity> saplingItems = world.getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(pos).grow(SAPLING_PICK_RANGE), entityItem -> entityItem.getItem().getItem() == saplingBlock.asItem());
                if (!saplingItems.isEmpty()){
                    saplingItems.get(0).getItem().shrink(1); // Use a sapling
                    world.setBlockState(pos, saplingState);  // And plant it.
                    return true;
                }
            }
        }

        return false;
    }
}
