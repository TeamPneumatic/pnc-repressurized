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

import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class BandageItem extends Item {
    public BandageItem() {
        super(ModItems.defaultProps().stacksTo(16));
    }

    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {
        if (entityLiving instanceof Player player) {
            player.setHealth(player.getHealth() + ConfigHelper.common().general.bandageHealthRestored.get().floatValue());
            stack.shrink(1);
            int cooldown = ConfigHelper.common().general.bandageCooldown.get();
            if (cooldown > 0) {
                player.getCooldowns().addCooldown(stack.getItem(), cooldown);
            }
            if (worldIn.isClientSide) {
                Vec3 pos = player.getEyePosition(1f).add(player.getLookAngle().scale(0.5));
                for (int i = 0; i < 5; i++) {
                    worldIn.addParticle(ParticleTypes.HEART, pos.x + worldIn.random.nextFloat() - 0.5, pos.y + worldIn.random.nextFloat() - 0.5, pos.z + worldIn.random.nextFloat() - 0.5, 0, 0.05, 0);
                }
            }
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return ConfigHelper.common().general.bandageUseTime.get();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }
}
