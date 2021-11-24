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
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberValve;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberWall;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public abstract class BlockPressureChamberWallBase extends BlockPneumaticCraft implements IBlockPressureChamber {
    BlockPressureChamberWallBase(Properties props) {
        super(props);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPressureChamberWall.class;
    }

    @Override
    public void setPlacedBy(World par1World, BlockPos pos, BlockState state, LivingEntity par5EntityLiving, ItemStack iStack) {
        super.setPlacedBy(par1World, pos, state, par5EntityLiving, iStack);
        if (!par1World.isClientSide && TileEntityPressureChamberValve.checkIfProperlyFormed(par1World, pos)) {
            AdvancementTriggers.PRESSURE_CHAMBER.trigger((ServerPlayerEntity) par5EntityLiving);
        }
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        if (world.isClientSide) return ActionResultType.SUCCESS;
        // forward activation to the pressure chamber valve, which will open the GUI
        return PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityPressureChamberWall.class).map(te -> {
            TileEntityPressureChamberValve valve = te.getCore();
            if (valve != null) {
                NetworkHooks.openGui((ServerPlayerEntity) player, valve, valve.getBlockPos());
                return ActionResultType.CONSUME;
            }
            return ActionResultType.FAIL;
        }).orElse(ActionResultType.FAIL);
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock() && !world.isClientSide) {
            PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityPressureChamberWall.class)
                    .ifPresent(TileEntityPressureChamberWall::onBlockBreak);
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }
}
