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

package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.api.block.PNCBlockStateProperties;
import me.desht.pneumaticcraft.api.block.PressureChamberWallState;
import me.desht.pneumaticcraft.common.block.entity.processing.PressureChamberValveBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.processing.PressureChamberWallBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModCriterionTriggers;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import static me.desht.pneumaticcraft.api.block.PNCBlockStateProperties.FORMED;

public abstract class AbstractPressureWallBlock extends AbstractPneumaticCraftBlock implements IBlockPressureChamber, PneumaticCraftEntityBlock {

    AbstractPressureWallBlock(Properties props) {
        super(props);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PressureChamberWallBlockEntity(pPos, pState);
    }

    @Override
    public void setPlacedBy(Level par1World, BlockPos pos, BlockState state, LivingEntity par5EntityLiving, ItemStack iStack) {
        super.setPlacedBy(par1World, pos, state, par5EntityLiving, iStack);
        if (!par1World.isClientSide && PressureChamberValveBlockEntity.checkIfProperlyFormed(par1World, pos)) {
            ModCriterionTriggers.PRESSURE_CHAMBER.get().trigger((ServerPlayer) par5EntityLiving);
        }
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult brtr) {
        if (player instanceof ServerPlayer sp) {
            // forward activation to the pressure chamber valve, which will open the GUI
            return PneumaticCraftUtils.getBlockEntityAt(world, pos, PressureChamberWallBlockEntity.class).map(te -> {
                PressureChamberValveBlockEntity valve = te.getPrimaryValve();
                if (valve != null) {
                    sp.openMenu(valve, valve.getBlockPos());
                    return InteractionResult.CONSUME;
                }
                return InteractionResult.FAIL;
            }).orElse(InteractionResult.FAIL);
        } else {
            return isFormed(state) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
    }

    private boolean isFormed(BlockState state) {
        if (state.hasProperty(PNCBlockStateProperties.WALL_STATE)) {
            return state.getValue(PNCBlockStateProperties.WALL_STATE) != PressureChamberWallState.NONE;
        } else if (state.hasProperty(FORMED)) {
            return state.getValue(FORMED);
        } else {
            return false;
        }
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock() && !world.isClientSide) {
            PneumaticCraftUtils.getBlockEntityAt(world, pos, PressureChamberWallBlockEntity.class)
                    .ifPresent(PressureChamberWallBlockEntity::onBlockBreak);
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }
}
