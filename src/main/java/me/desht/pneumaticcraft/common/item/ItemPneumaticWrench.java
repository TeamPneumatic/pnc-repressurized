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
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemPneumaticWrench extends ItemPressurizable {

    public ItemPneumaticWrench() {
        super(ModItems.toolProps(), PneumaticValues.PNEUMATIC_WRENCH_MAX_AIR, PneumaticValues.PNEUMATIC_WRENCH_VOLUME);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext ctx) {
        Hand hand = ctx.getHand();
        World world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        if (!world.isClientSide) {
            BlockState state = world.getBlockState(pos);
            ActionResultType res = ModdedWrenchUtils.getInstance().onWrenchedPre(ctx, state);
            if (res != ActionResultType.PASS) {
                if (res == ActionResultType.SUCCESS) playWrenchSound(world, pos);
                return res;
            }
            IAirHandler airHandler = stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).orElseThrow(RuntimeException::new);
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
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.PASS;
        } else {
            // client-side: prevent GUI's opening etc.
            return ActionResultType.SUCCESS;
        }
    }

    private void playWrenchSound(World world, BlockPos pos) {
        NetworkHandler.sendToAllTracking(new PacketPlaySound(ModSounds.PNEUMATIC_WRENCH.get(), SoundCategory.PLAYERS, pos, 1.0F, 1.0F, true), world, pos);
    }

    @Override
    public ActionResultType interactLivingEntity(ItemStack iStack, PlayerEntity player, LivingEntity target, Hand hand) {
        if (player.level.isClientSide) {
            return ActionResultType.SUCCESS;
        } else if (target.isAlive() && target instanceof IPneumaticWrenchable) {
            return iStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).map(h -> {
                if (!player.isCreative() && h.getAir() < PneumaticValues.USAGE_PNEUMATIC_WRENCH) {
                    return ActionResultType.FAIL;
                }
                if (((IPneumaticWrenchable) target).onWrenched(target.level, player, null, null, hand)) {
                    if (!player.isCreative()) {
                        h.addAir(-PneumaticValues.USAGE_PNEUMATIC_WRENCH);
                    }
                    playWrenchSound(target.level, target.blockPosition());
                    return ActionResultType.SUCCESS;
                } else {
                    return ActionResultType.PASS;
                }
            }).orElseThrow(RuntimeException::new);
        }
        return ActionResultType.PASS;
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (entityIn instanceof ServerPlayerEntity && stack.getItem() instanceof IPressurizableItem
                && ((IPressurizableItem) stack.getItem()).getPressure(stack) >= 3f) {
            AdvancementTriggers.CHARGED_WRENCH.trigger((ServerPlayerEntity) entityIn);
        }
    }
}
