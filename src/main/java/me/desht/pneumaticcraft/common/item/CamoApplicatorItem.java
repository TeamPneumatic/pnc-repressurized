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
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import me.desht.pneumaticcraft.common.block.entity.CamouflageableBlockEntity;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class CamoApplicatorItem extends PressurizableItem {
    public CamoApplicatorItem() {
        super(ModItems.toolProps(), PneumaticValues.PNEUMATIC_WRENCH_MAX_AIR, PneumaticValues.PNEUMATIC_WRENCH_VOLUME);
    }

    @Override
    public Component getName(ItemStack stack) {
        BlockState camoState = getCamoState(stack);
        Component disp = super.getName(stack);
        return camoState != null ?
                disp.copy().append(": ").append(getCamoStateDisplayName(camoState)).withStyle(ChatFormatting.YELLOW) :
                disp;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if (playerIn.isShiftKeyDown()) {
            if (!worldIn.isClientSide) {
                setCamoState(playerIn.getItemInHand(handIn), null);
            } else {
                if (getCamoState(playerIn.getItemInHand(handIn)) != null) {
                    playerIn.playSound(ModSounds.CHIRP.get(), 1.0f, 1.0f);
                }
            }
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getItemInHand(handIn));
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, playerIn.getItemInHand(handIn));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Player player = ctx.getPlayer();

        if (player != null && !level.isClientSide) {
            if (player.isCrouching()) {
                // sneak-right-click: clear camo
                setCamoState(stack, null);
                level.playSound(null, ctx.getClickedPos(), ModSounds.CHIRP.get(), SoundSource.PLAYERS, 1f, 1f);
            } else {
                BlockEntity te = level.getBlockEntity(pos);
                BlockState state = level.getBlockState(pos);
                if (!(te instanceof CamouflageableBlockEntity camoTE)) {
                    // right-click non-camo block: copy its state
                    setCamoState(stack, state);
                    level.playSound(null, ctx.getClickedPos(), ModSounds.CHIRP.get(), SoundSource.PLAYERS, 1f, 2f);
                } else {
                    // right-click camo block: try to apply (or remove) camo

                    IAirHandlerItem airHandler = PNCCapabilities.getAirHandler(stack).orElseThrow(RuntimeException::new);
                    if (!player.isCreative() && airHandler.getPressure() < 0.1F) {
                        // not enough pressure
                        return InteractionResult.FAIL;
                    }

                    BlockState newCamo = getCamoState(stack);
                    BlockState existingCamo = camoTE.getCamouflage();

                    if (existingCamo == newCamo) {
                        level.playSound(null, ctx.getClickedPos(), SoundEvents.COMPARATOR_CLICK, SoundSource.PLAYERS, 1f, 2f);
                        return InteractionResult.SUCCESS;
                    }

                    // make sure player has enough of the camo item
                    if (newCamo != null && !player.isCreative()) {
                        ItemStack camoStack = CamouflageableBlockEntity.getStackForState(newCamo);
                        if (!PneumaticCraftUtils.consumeInventoryItem(player.getInventory(), camoStack)) {
                            player.displayClientMessage(xlate("pneumaticcraft.message.camo.notEnoughBlocks", camoStack.getHoverName())
                                    .withStyle(ChatFormatting.RED), true);
                            NetworkHandler.sendToAllTracking(new PacketPlaySound(ModSounds.MINIGUN_STOP.get(), SoundSource.PLAYERS,
                                    pos, 1.0F, 2.0F, true), level, pos);
                            return InteractionResult.FAIL;
                        }
                    }

                    // return existing camo block, if any
                    if (existingCamo != null && !player.isCreative()) {
                        ItemStack camoStack = CamouflageableBlockEntity.getStackForState(existingCamo);
                        ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, camoStack);
                        level.addFreshEntity(entity);
                        entity.playerTouch(player);
                    }

                    // and apply the new camouflage
                    if (!player.isCreative()) {
                        airHandler.addAir(-PneumaticValues.USAGE_CAMO_APPLICATOR);
                    }
                    camoTE.setCamouflage(newCamo);
                    BlockState particleState = newCamo == null ? existingCamo : newCamo;
                    if (particleState != null) {
                        player.getCommandSenderWorld().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(particleState));
                    }
                    NetworkHandler.sendToAllTracking(new PacketPlaySound(ModSounds.SHORT_HISS.get(), SoundSource.PLAYERS, pos, 1.0F, 1.0F, true), level, pos);
                }
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void setCamoState(ItemStack stack, BlockState state) {
        if (state == null) {
            stack.remove(ModDataComponents.CAMO_STATE);
        } else {
            stack.set(ModDataComponents.CAMO_STATE, state);
        }
    }

    private static BlockState getCamoState(ItemStack stack) {
        return stack.get(ModDataComponents.CAMO_STATE);
    }

    public static Component getCamoStateDisplayName(BlockState state) {
        return state != null ?
                new ItemStack(state.getBlock().asItem()).getHoverName() :
                Component.literal("<?>");
    }

}
