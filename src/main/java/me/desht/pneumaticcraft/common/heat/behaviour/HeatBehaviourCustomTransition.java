package me.desht.pneumaticcraft.common.heat.behaviour;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.config.aux.BlockHeatPropertiesConfig;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class HeatBehaviourCustomTransition extends HeatBehaviourTransition {
    private static final ResourceLocation ID = RL("custom_transition");

    private BlockHeatPropertiesConfig.CustomHeatEntry heatEntry;

    @Override
    public void initialize(IHeatExchangerLogic connectedHeatLogic, World world, BlockPos pos, Direction direction) {
        super.initialize(connectedHeatLogic, world, pos, direction);

        heatEntry = BlockHeatPropertiesConfig.INSTANCE.getCustomHeatEntry(getBlockState().getBlock());
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public boolean isApplicable() {
        if (!super.isApplicable()) return false;

        return heatEntry != null && heatEntry.getTotalHeatCapacity() != 0;
    }

    @Override
    protected int getMaxExchangedHeat() {
        return getHeatEntry().getTotalHeatCapacity();
    }

    @Override
    protected boolean transformBlockHot() {
        Block hot = getHeatEntry().getTransformHot();
        if (hot == null) return false;
        if (getFluid() != null) {
            transformFluidBlocks(hot.getDefaultState(), getHeatEntry().getTransformHotFlowing().getDefaultState());
            return true;
        } else {
            return getWorld().setBlockState(getPos(), hot.getDefaultState());
        }
    }

    @Override
    protected boolean transformBlockCold() {
        Block cold = getHeatEntry().getTransformCold();
        if (cold == null) return false;
        if (getFluid() != null) {
            transformFluidBlocks(cold.getDefaultState(), getHeatEntry().getTransformColdFlowing().getDefaultState());
            return true;
        } else {
            return getWorld().setBlockState(getPos(), cold.getDefaultState());
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
        return b1 == b2;
//                || b1 == Blocks.FLOWING_LAVA && b2 == Blocks.LAVA
//                || b1 == Blocks.LAVA && b2 == Blocks.FLOWING_LAVA
//                || b1 == Blocks.FLOWING_WATER && b2 == Blocks.WATER
//                || b1 == Blocks.WATER && b2 == Blocks.FLOWING_WATER;
    }
}
