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
import me.desht.pneumaticcraft.common.block.entity.processing.PressureChamberValveBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModCriterionTriggers;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class PressureChamberValveBlock extends AbstractPneumaticCraftBlock implements IBlockPressureChamber, PneumaticCraftEntityBlock {
    public PressureChamberValveBlock() {
        super(ModBlocks.defaultProps());
        registerDefaultState(defaultBlockState().setValue(PNCBlockStateProperties.FORMED, false));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity livingEntity, ItemStack iStack) {
        super.setPlacedBy(level, pos, state, livingEntity, iStack);

        if (livingEntity instanceof ServerPlayer sp && PressureChamberValveBlockEntity.checkIfProperlyFormed(level, pos)) {
            ModCriterionTriggers.PRESSURE_CHAMBER.get().trigger(sp);
        }
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom() {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PNCBlockStateProperties.FORMED);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult brtr) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer sp) {
            return world.getBlockEntity(pos, ModBlockEntityTypes.PRESSURE_CHAMBER_VALVE.get()).map(te -> {
                if (te.multiBlockSize > 0) {
                    sp.openMenu(te, pos);
                } else if (!te.accessoryValves.isEmpty()) {
                    // when this isn't the core valve, track down the core valve
                    for (PressureChamberValveBlockEntity valve : te.accessoryValves) {
                        if (valve.multiBlockSize > 0) {
                            sp.openMenu(valve, valve.getBlockPos());
                            break;
                        }
                    }
                } else {
                    return InteractionResult.PASS;
                }
                return InteractionResult.SUCCESS;
            }).orElse(InteractionResult.SUCCESS);
        } else {
            return world.getBlockEntity(pos, ModBlockEntityTypes.PRESSURE_CHAMBER_VALVE.get())
                    .filter(te -> te.multiBlockSize > 0)
                    .map(te -> InteractionResult.SUCCESS)
                    .orElse(InteractionResult.PASS);
        }
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            invalidateMultiBlock(world, pos);
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    private void invalidateMultiBlock(Level world, BlockPos pos) {
        if (!world.isClientSide) {
            PneumaticCraftUtils.getBlockEntityAt(world, pos, PressureChamberValveBlockEntity.class).ifPresent(teValve -> {
                if (teValve.multiBlockSize > 0) {
                    teValve.onMultiBlockBreak();
                } else if (teValve.accessoryValves.size() > 0) {
                    teValve.accessoryValves.stream()
                            .filter(valve -> valve.multiBlockSize > 0)
                            .findFirst()
                            .ifPresent(PressureChamberValveBlockEntity::onMultiBlockBreak);
                }
            });
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PressureChamberValveBlockEntity(pPos, pState);
    }
}
