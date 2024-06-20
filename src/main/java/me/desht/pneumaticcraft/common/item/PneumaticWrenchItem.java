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

package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.block.IPneumaticWrenchable;
import me.desht.pneumaticcraft.api.pressure.IPressurizableItem;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.registry.ModCriterionTriggers;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

public class PneumaticWrenchItem extends PressurizableItem {

    public PneumaticWrenchItem() {
        super(ModItems.toolProps(), PneumaticValues.PNEUMATIC_WRENCH_MAX_AIR, PneumaticValues.PNEUMATIC_WRENCH_VOLUME);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
        InteractionHand hand = ctx.getHand();
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        if (!world.isClientSide) {
            BlockState state = world.getBlockState(pos);
            InteractionResult res = ModdedWrenchUtils.getInstance().onWrenchedPre(ctx, state);
            if (res != InteractionResult.PASS) {
                if (res == InteractionResult.SUCCESS) playWrenchSound(world, pos);
                return res;
            }
            IAirHandler airHandler = PNCCapabilities.getAirHandler(stack).orElseThrow(RuntimeException::new);
            boolean didWork = false;
            float pressure = airHandler.getPressure();
            IPneumaticWrenchable wrenchable = IPneumaticWrenchable.forBlock(state.getBlock());
            if (wrenchable != null && pressure > 0.1f) {
                if (wrenchable.onWrenched(world, ctx.getPlayer(), pos, ctx.getClickedFace(), hand)) {
                    if (ctx.getPlayer() != null && !ctx.getPlayer().isCreative()) {
                        airHandler.addAir(-PneumaticValues.USAGE_PNEUMATIC_WRENCH);
                    }
                    didWork = true;
                }
            } else {
                // rotating normal blocks doesn't use pressure
                BlockState rotated = state.rotate(world, pos, Rotation.CLOCKWISE_90);
                if (rotated != state) {
                    world.setBlockAndUpdate(pos, rotated);
                    didWork = true;
                    state = rotated;
                }
            }
            if (didWork) {
                playWrenchSound(world, pos);
                ModdedWrenchUtils.getInstance().onWrenchedPost(ctx, state);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        } else {
            // client-side: prevent GUI's opening etc.
            return InteractionResult.SUCCESS;
        }
    }

    private void playWrenchSound(Level world, BlockPos pos) {
        NetworkHandler.sendToAllTracking(new PacketPlaySound(ModSounds.PNEUMATIC_WRENCH.get(), SoundSource.PLAYERS, pos, 1.0F, 1.0F, true), world, pos);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack iStack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        } else if (target.isAlive() && target instanceof IPneumaticWrenchable) {
            return PNCCapabilities.getAirHandler(iStack).map(h -> {
                if (!player.isCreative() && h.getAir() < PneumaticValues.USAGE_PNEUMATIC_WRENCH) {
                    return InteractionResult.FAIL;
                }
                if (((IPneumaticWrenchable) target).onWrenched(target.level(), player, null, null, hand)) {
                    if (!player.isCreative()) {
                        h.addAir(-PneumaticValues.USAGE_PNEUMATIC_WRENCH);
                    }
                    playWrenchSound(target.level(), target.blockPosition());
                    return InteractionResult.SUCCESS;
                } else {
                    return InteractionResult.PASS;
                }
            }).orElseThrow(RuntimeException::new);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (entityIn instanceof ServerPlayer sp && stack.getItem() instanceof PneumaticWrenchItem pw && pw.getPressure(stack) >= 3f) {
            ModCriterionTriggers.CHARGED_WRENCH.get().trigger(sp);
        }
    }
}
