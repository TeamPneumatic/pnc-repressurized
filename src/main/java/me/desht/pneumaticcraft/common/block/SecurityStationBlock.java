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

import me.desht.pneumaticcraft.common.block.entity.utility.SecurityStationBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SecurityStationBlock extends AbstractPneumaticCraftBlock implements PneumaticCraftEntityBlock {
    private static final VoxelShape SHAPE_N = VoxelShapeUtils.or(
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
    );
    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape SHAPE_S = VoxelShapeUtils.rotateY(SHAPE_E, 90);
    private static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_S, 90);
    private static final VoxelShape[] SHAPES = new VoxelShape[] { SHAPE_S, SHAPE_W, SHAPE_N, SHAPE_E };

    public SecurityStationBlock() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Direction d = state.getValue(directionProperty());
        return SHAPES[d.get2DDataValue()];
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity entityLiving, ItemStack iStack) {
        if (entityLiving instanceof Player p) {
            world.getBlockEntity(pos, ModBlockEntityTypes.SECURITY_STATION.get())
                    .ifPresent(teSS -> teSS.sharedUsers.add(p.getGameProfile()));
        }

        super.setPlacedBy(world, pos, state, entityLiving, iStack);
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult brtr) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer sp && world.getBlockEntity(pos) instanceof SecurityStationBlockEntity secStation) {
            if (secStation.isPlayerOnWhiteList(player)) {
                return super.use(state, world, pos, player, hand, brtr);
            } else if (!secStation.hasValidNetwork()) {
                player.displayClientMessage(PneumaticCraftUtils.xlate("pneumaticcraft.message.securityStation.outOfOrder").withStyle(ChatFormatting.RED), false);
            } else if (secStation.hasPlayerHacked(player)) {
                player.displayClientMessage(PneumaticCraftUtils.xlate("pneumaticcraft.message.securityStation.alreadyHacked").withStyle(ChatFormatting.GOLD), false);
            } else if (getPlayerHackLevel(player) < secStation.getSecurityLevel()) {
                player.displayClientMessage(PneumaticCraftUtils.xlate("pneumaticcraft.message.securityStation.cantHack").withStyle(ChatFormatting.GOLD), false);
                player.hurt(player.damageSources().fellOutOfWorld(), 1f);
            } else {
                if (ConfigHelper.common().machines.securityStationAllowHacking.get()) {
                    secStation.initiateHacking(sp);
                } else {
                    player.displayClientMessage(PneumaticCraftUtils.xlate("pneumaticcraft.message.securityStation.hackDisabled").withStyle(ChatFormatting.GOLD), false);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    private int getPlayerHackLevel(Player player) {
        CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
        return handler.isArmorReady(EquipmentSlot.HEAD) && handler.getArmorPressure(EquipmentSlot.HEAD) > 0f ?
                handler.getUpgradeCount(EquipmentSlot.HEAD, ModUpgrades.SECURITY.get()) : 0;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockAccess.getBlockEntity(pos, ModBlockEntityTypes.SECURITY_STATION.get())
                .map(teSS -> teSS.getRedstoneController().shouldEmit() ? 15 : 0).orElse(0);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SecurityStationBlockEntity(pPos, pState);
    }
}
