package me.desht.pneumaticcraft.common.heat.behaviour;

import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.heat.BlockHeatProperties;
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

import static me.desht.pneumaticcraft.common.heat.BlockHeatProperties.CustomHeatEntry;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class HeatBehaviourCustomTransition extends HeatBehaviourTransition {
    static final ResourceLocation ID = RL("custom_transition");

    private CustomHeatEntry heatEntry;

    @Override
    public HeatBehaviour initialize(IHeatExchangerLogic connectedHeatLogic, World world, BlockPos pos, Direction direction) {
        super.initialize(connectedHeatLogic, world, pos, direction);

        heatEntry = BlockHeatProperties.getInstance().getCustomHeatEntry(getBlockState().getBlock());
        return this;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public boolean isApplicable() {
        if (!super.isApplicable()) return false;

        return heatEntry != null && heatEntry.getHeatCapacity() != 0;
    }

    @Override
    protected int getMaxExchangedHeat() {
        return getHeatEntry().getHeatCapacity();
    }

    @Override
    protected boolean transformBlockHot() {
        BlockState hot = getHeatEntry().getTransformHot();
        if (hot != null) {
            if (getFluid() != null) {
                transformFluidBlocks(hot, getHeatEntry().getTransformHotFlowing());
                return true;
            } else {
                return getWorld().setBlockState(getPos(), hot);
            }
        } else {
            return false;
        }
    }

    @Override
    protected boolean transformBlockCold() {
        BlockState cold = getHeatEntry().getTransformCold();
        if (cold != null) {
            if (getFluid() != null) {
                transformFluidBlocks(cold, getHeatEntry().getTransformColdFlowing());
                return true;
            } else {
                return getWorld().setBlockState(getPos(), cold);
            }
        } else {
            return false;
        }
    }

    private CustomHeatEntry getHeatEntry() {
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
                    if (checkingBlock == getBlockState().getBlock() && traversed.add(newPos)) {
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
}
