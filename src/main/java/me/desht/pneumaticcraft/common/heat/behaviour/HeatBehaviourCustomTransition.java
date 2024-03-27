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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class HeatBehaviourCustomTransition extends HeatBehaviourTransition {
    static final ResourceLocation ID = RL("custom_transition");

    private HeatPropertiesRecipe heatEntry;

    @Override
    public HeatBehaviour initialize(IHeatExchangerLogic connectedHeatLogic, Level world, BlockPos pos, Direction direction) {
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
        return super.isApplicable() && heatEntry != null && heatEntry.getHeatCapacity().orElse(0) > 0;
    }

    @Override
    protected int getMaxExchangedHeat() {
        return heatEntry.getHeatCapacity().orElse(0);
    }

    @Override
    protected boolean transformBlockHot() {
        return heatEntry.getTransformHot().map(hot -> {
            if (getFluid() != null) {
                transformFluidBlocks(hot, heatEntry.getTransformHotFlowing().orElse(Blocks.AIR.defaultBlockState()));
                return true;
            } else {
                return getWorld().setBlockAndUpdate(getPos(), hot);
            }
        }).orElse(false);
    }

    @Override
    protected boolean transformBlockCold() {
        return heatEntry.getTransformCold().map(cold -> {
            if (getFluid() != null) {
                transformFluidBlocks(cold, heatEntry.getTransformColdFlowing().orElse(Blocks.AIR.defaultBlockState()));
                return true;
            } else {
                return getWorld().setBlockAndUpdate(getPos(), cold);
            }
        }).orElse(false);
    }

    /**
     * Transform a fluid block into some other block, following flowing fluids back to the source block where
     * necessary.
     *
     * @param turningBlockSource blockstate to transform the source block to
     * @param turningBlockFlowing blockstate to transform any flowing blocks to
     */
    private void transformFluidBlocks(@NotNull BlockState turningBlockSource, @NotNull BlockState turningBlockFlowing) {
        if (FluidUtils.isSourceFluidBlock(getWorld(), getPos())) {
            getWorld().setBlockAndUpdate(getPos(), turningBlockSource);
            onTransition(getPos());
        } else {
            // a flowing block: follow it back to the source
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
