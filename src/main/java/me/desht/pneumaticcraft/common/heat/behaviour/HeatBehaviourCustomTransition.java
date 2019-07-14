package me.desht.pneumaticcraft.common.heat.behaviour;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.config.BlockHeatPropertiesConfig;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class HeatBehaviourCustomTransition extends HeatBehaviourTransition {
    private BlockHeatPropertiesConfig.CustomHeatEntry heatEntry;

    @Override
    public void initialize(String id, IHeatExchangerLogic connectedHeatLogic, World world, BlockPos pos, Direction direction) {
        super.initialize(id, connectedHeatLogic, world, pos, direction);

        heatEntry = BlockHeatPropertiesConfig.INSTANCE.getCustomHeatEntry(getBlockState());
    }

    @Override
    public String getId() {
        return Names.MOD_ID + ":customTransition";
    }

    @Override
    public boolean isApplicable() {
        if (!super.isApplicable()) return false;

        BlockHeatPropertiesConfig.CustomHeatEntry entry = getHeatEntry();
        return getHeatEntry() != null && getHeatEntry().getTotalHeat() != 0;
    }

    @Override
    protected int getMaxExchangedHeat() {
        return getHeatEntry().getTotalHeat();
    }

    @Override
    protected boolean transformBlockHot() {
        BlockState hot = getHeatEntry().getTransformHot();
        if (hot == null) return false;
        if (getFluid() != null) {
            transformFluidBlocks(hot, getHeatEntry().getTransformHotFlowing());
            return true;
        } else {
            return getWorld().setBlockState(getPos(), hot);
        }
    }

    @Override
    protected boolean transformBlockCold() {
        BlockState cold = getHeatEntry().getTransformCold();
        if (cold == null) return false;
        if (getFluid() != null) {
            transformFluidBlocks(cold, getHeatEntry().getTransformColdFlowing());
            return true;
        } else {
            return getWorld().setBlockState(getPos(), cold);
        }
    }

    private BlockHeatPropertiesConfig.CustomHeatEntry getHeatEntry() {
        return heatEntry;
    }

    /**
     * Transform a fluid block into some other block, following flowing fluids back to the source block where
     * necessary.
     *
     * @param turningBlockSource blockstate to transform the source block to
     * @param turningBlockFlowing blockstate to transform any flowing blocks to
     */
    private void transformFluidBlocks(BlockState turningBlockSource, BlockState turningBlockFlowing) {
        if (FluidUtils.isSourceBlock(getWorld(), getPos())) {
            getWorld().setBlockState(getPos(), turningBlockSource);
        } else {
            Set<BlockPos> traversed = new HashSet<>();
            Stack<BlockPos> pending = new Stack<>();
            pending.push(getPos());
            traversed.add(getPos());
            while (!pending.isEmpty()) {
                BlockPos pos = pending.pop();
                for (Direction d : Direction.VALUES) {
                    BlockPos newPos = pos.offset(d);
                    Block checkingBlock = getWorld().getBlockState(newPos).getBlock();
                    if (blocksSame(checkingBlock, getBlockState().getBlock()) && traversed.add(newPos)) {
                        if (FluidUtils.isSourceBlock(getWorld(), newPos)) {
                            getWorld().setBlockState(newPos, turningBlockSource);
                            onTransition(newPos);
                            return;
                        } else {
                            getWorld().setBlockState(newPos, turningBlockFlowing);
                            onTransition(newPos);
                            pending.push(newPos);
                        }
                    }
                }
            }
        }
    }

    private boolean blocksSame(Block b1, Block b2) {
        return b1 == b2
                || b1 == Blocks.FLOWING_LAVA && b2 == Blocks.LAVA
                || b1 == Blocks.LAVA && b2 == Blocks.FLOWING_LAVA
                || b1 == Blocks.FLOWING_WATER && b2 == Blocks.WATER
                || b1 == Blocks.WATER && b2 == Blocks.FLOWING_WATER;
    }
}
