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

import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberValve;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class BlockPressureChamberValve extends BlockPneumaticCraft implements IBlockPressureChamber {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public BlockPressureChamberValve() {
        super(ModBlocks.defaultProps());
        registerDefaultState(getStateDefinition().any().setValue(FORMED, false));
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPressureChamberValve.class;
    }

    @Override
    public void setPlacedBy(World par1World, BlockPos pos, BlockState state, LivingEntity par5EntityLiving, ItemStack iStack) {
        super.setPlacedBy(par1World, pos, state, par5EntityLiving, iStack);
        if (!par1World.isClientSide && TileEntityPressureChamberValve.checkIfProperlyFormed(par1World, pos)) {
            AdvancementTriggers.PRESSURE_CHAMBER.trigger((ServerPlayerEntity) par5EntityLiving);
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
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FORMED);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        if (player.isShiftKeyDown()) {
            return ActionResultType.PASS;
        }
        if (!world.isClientSide) {
            return PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityPressureChamberValve.class).map(te -> {
                if (te.multiBlockSize > 0) {
                    NetworkHooks.openGui((ServerPlayerEntity) player, te, pos);
                } else if (te.accessoryValves.size() > 0) {
                    // when this isn't the core valve, track down the core valve
                    for (TileEntityPressureChamberValve valve : te.accessoryValves) {
                        if (valve.multiBlockSize > 0) {
                            NetworkHooks.openGui((ServerPlayerEntity) player, valve, valve.getBlockPos());
                            break;
                        }
                    }
                } else {
                    return ActionResultType.PASS;
                }
                return ActionResultType.SUCCESS;
            }).orElse(ActionResultType.SUCCESS);
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            invalidateMultiBlock(world, pos);
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    private void invalidateMultiBlock(World world, BlockPos pos) {
        if (!world.isClientSide) {
            PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityPressureChamberValve.class).ifPresent(teValve -> {
                if (teValve.multiBlockSize > 0) {
                    teValve.onMultiBlockBreak();
                } else if (teValve.accessoryValves.size() > 0) {
                    teValve.accessoryValves.stream()
                            .filter(valve -> valve.multiBlockSize > 0)
                            .findFirst()
                            .ifPresent(TileEntityPressureChamberValve::onMultiBlockBreak);
                }
            });
        }
    }
}
