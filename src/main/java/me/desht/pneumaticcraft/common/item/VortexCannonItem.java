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
import me.desht.pneumaticcraft.common.entity.projectile.VortexEntity;
import me.desht.pneumaticcraft.common.registry.ModEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class VortexCannonItem extends PressurizableItem {

    public VortexCannonItem() {
        super(ModItems.toolProps(), PneumaticValues.VORTEX_CANNON_MAX_AIR, PneumaticValues.VORTEX_CANNON_VOLUME);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player playerIn, InteractionHand handIn) {
        ItemStack iStack = playerIn.getItemInHand(handIn);

        IAirHandlerItem airHandler = PNCCapabilities.getAirHandler(iStack).orElseThrow(RuntimeException::new);
        float factor = 0.2F * airHandler.getPressure();

        if (world.isClientSide) {
            if (airHandler.getPressure() > 0.1f) {
                world.playLocalSound(playerIn.getX(), playerIn.getY(), playerIn.getZ(), ModSounds.AIR_CANNON.get(), SoundSource.PLAYERS, 1.0F, 0.7F + factor * 0.2F, false);
            } else {
                playerIn.playSound(SoundEvents.COMPARATOR_CLICK, 1.0f, 2f);
                return InteractionResultHolder.fail(iStack);
            }
        } else {
            if (airHandler.getPressure() > 0.1f) {
                world.playSound(playerIn, playerIn.getX(), playerIn.getY(), playerIn.getZ(), ModSounds.AIR_CANNON.get(), SoundSource.PLAYERS, 1.0F, 0.7F + factor * 0.2F);
                VortexEntity vortex = ModEntityTypes.VORTEX.get().create(world);
                if (vortex != null) {
                    Vec3 directionVec = playerIn.getLookAngle().normalize().scale(playerIn.isSprinting() ? -0.35 : -0.15);
                    Vec3 vortexPos = playerIn.position().add(0, playerIn.getEyeHeight() / 2, 0).add(directionVec);
                    vortex.setPos(vortexPos.x, vortexPos.y, vortexPos.z);
                    vortex.shootFromRotation(playerIn, playerIn.getXRot(), playerIn.getYRot(), 0.0F, 1.5F * factor, 0.0F);
                    world.addFreshEntity(vortex);
                    if (!playerIn.isCreative()) {
                        airHandler.addAir(-PneumaticValues.USAGE_VORTEX_CANNON);
                    }
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(iStack, world.isClientSide);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }
}
