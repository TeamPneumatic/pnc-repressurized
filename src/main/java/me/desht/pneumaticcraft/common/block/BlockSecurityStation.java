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

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.stream.Stream;

public class BlockSecurityStation extends BlockPneumaticCraft {
    private static final VoxelShape SHAPE_N = Stream.of(
            Block.box(0, 8, 0, 16, 10, 16),
            Block.box(13, 0, 13, 15, 8, 15),
            Block.box(1, 0, 13, 3, 8, 15),
            Block.box(1, 10, 9, 11, 11, 15),
            Block.box(1, 10, 1, 15, 11, 7),
            Block.box(12, 10, 11, 15, 11, 15),
            Block.box(12.25, 10, 7, 12.75, 10.5, 11),
            Block.box(9.25, 10, 7, 9.75, 10.5, 9),
            Block.box(3, 10.5, 2, 13, 14.5, 4),
            Block.box(1, 0, 1, 3, 8, 3),
            Block.box(13, 0, 1, 15, 8, 3),
            Block.box(13.25, 10, 7, 13.75, 10.5, 11)
    ).reduce((v1, v2) -> VoxelShapes.join(v1, v2, IBooleanFunction.OR)).get();
    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape SHAPE_S = VoxelShapeUtils.rotateY(SHAPE_E, 90);
    private static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_S, 90);
    private static final VoxelShape[] SHAPES = new VoxelShape[] { SHAPE_S, SHAPE_W, SHAPE_N, SHAPE_E };

    public BlockSecurityStation() {
        super(ModBlocks.defaultProps());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction d = state.getValue(directionProperty());
        return SHAPES[d.get2DDataValue()];
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntitySecurityStation.class;
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entityLiving, ItemStack iStack) {
        if (entityLiving instanceof PlayerEntity) {
            PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntitySecurityStation.class)
                    .ifPresent(te -> te.sharedUsers.add(((PlayerEntity) entityLiving).getGameProfile()));
        }

        super.setPlacedBy(world, pos, state, entityLiving, iStack);
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        if (player.isShiftKeyDown()) {
            return ActionResultType.PASS;
        } else {
            if (!world.isClientSide) {
                TileEntity te = world.getBlockEntity(pos);
                if (te instanceof TileEntitySecurityStation) {
                    TileEntitySecurityStation teSS = (TileEntitySecurityStation) te;
                    if (teSS.isPlayerOnWhiteList(player)) {
                        return super.use(state, world, pos, player, hand, brtr);
                    } else if (!teSS.hasValidNetwork()) {
                        player.displayClientMessage(PneumaticCraftUtils.xlate("pneumaticcraft.message.securityStation.outOfOrder").withStyle(TextFormatting.RED), false);
                    } else if (teSS.hasPlayerHacked(player)) {
                        player.displayClientMessage(PneumaticCraftUtils.xlate("pneumaticcraft.message.securityStation.alreadyHacked").withStyle(TextFormatting.GOLD), false);
                    } else if (getPlayerHackLevel(player) < teSS.getSecurityLevel()) {
                        player.displayClientMessage(PneumaticCraftUtils.xlate("pneumaticcraft.message.securityStation.cantHack").withStyle(TextFormatting.GOLD), false);
                        player.hurt(DamageSource.OUT_OF_WORLD, 1f);
                    } else {
                        teSS.initiateHacking(player);
                    }
                }
            }
            return ActionResultType.SUCCESS;
        }
    }

    private int getPlayerHackLevel(PlayerEntity player) {
        CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
        return handler.isArmorReady(EquipmentSlotType.HEAD) && handler.getArmorPressure(EquipmentSlotType.HEAD) > 0f ?
                handler.getUpgradeCount(EquipmentSlotType.HEAD, EnumUpgrade.SECURITY) : 0;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return PneumaticCraftUtils.getTileEntityAt(blockAccess, pos, TileEntitySecurityStation.class)
                .map(teSS -> teSS.getRedstoneController().shouldEmit() ? 15 : 0).orElse(0);
    }
}
