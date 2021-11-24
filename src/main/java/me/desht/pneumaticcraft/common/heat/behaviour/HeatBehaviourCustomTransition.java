/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.heat.behaviour;

import me.desht.pneumaticcraft.api.crafting.recipe.HeatPropertiesRecipe;
import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.heat.BlockHeatProperties;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class HeatBehaviourCustomTransition extends HeatBehaviourTransition {
    static final ResourceLocation ID = RL("custom_transition");

    private HeatPropertiesRecipe heatEntry;

    @Override
    public HeatBehaviour<?> initialize(IHeatExchangerLogic connectedHeatLogic, World world, BlockPos pos, Direction direction) {
        super.initialize(connectedHeatLogic, world, pos, direction);

        heatEntry = BlockHeatProperties.getInstance().getCustomHeatEntry(world, getBlockState());
        return this;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public boolean isApplicable() {
        return super.isApplicable() && heatEntry != null && heatEntry.getHeatCapacity() > 0;
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
                return getWorld().setBlockAndUpdate(getPos(), hot);
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
                return getWorld().setBlockAndUpdate(getPos(), cold);
            }
        } else {
            return false;
        }
    }

    private HeatPropertiesRecipe getHeatEntry() {
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
        if (FluidUtils.isSourceFluidBlock(getWorld(), getPos())) {
            getWorld().setBlockAndUpdate(getPos(), turningBlockSource);
            onTransition(getPos());
        } else {
            // a flowing block: follow it back to the source
            if (turningBlockFlowing == null) turningBlockFlowing = Blocks.AIR.defaultBlockState();
            Set<BlockPos> traversed = new HashSet<>();
            Stack<BlockPos> pending = new Stack<>();
            pending.push(getPos());
            traversed.add(getPos());
            while (!pending.isEmpty()) {
                BlockPos pos = pending.pop();
                for (Direction d : DirectionUtil.VALUES) {
                    BlockPos newPos = pos.relative(d);
                    Block checkingBlock = getWorld().getBlockState(newPos).getBlock();
                    if (checkingBlock == getBlockState().getBlock() && traversed.add(newPos)) {
                        if (FluidUtils.isSourceFluidBlock(getWorld(), newPos)) {
                            getWorld().setBlockAndUpdate(newPos, turningBlockSource);
                            onTransition(newPos);
                            return;
                        } else {
                            getWorld().setBlockAndUpdate(newPos, turningBlockFlowing);
                            onTransition(newPos);
                            pending.push(newPos);
                        }
                    }
                }
            }
        }
    }
}
