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

package me.desht.pneumaticcraft.common.item.minigun;

import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class StandardGunAmmoItem extends AbstractGunAmmoItem {
    @Override
    public int getMaxDamage(ItemStack stack) {
        return ConfigHelper.common().minigun.standardAmmoCartridgeSize.get();
    }

    @Nonnull
    public static ItemStack getPotionStack(ItemStack ammo) {
        return ammo.has(ModDataComponents.POTION_AMMO) ?
                ammo.get(ModDataComponents.POTION_AMMO).copyOne() :
                ItemStack.EMPTY;
    }

    public static void setPotion(ItemStack ammo, ItemStack potion) {
        ammo.set(ModDataComponents.POTION_AMMO, ItemContainerContents.fromItems(List.of(potion)));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isFoil(ItemStack stack) {
        return stack.isEnchanted() || stack.has(ModDataComponents.POTION_AMMO);
    }

    @Override
    public int getAmmoCost(ItemStack ammoStack) {
        ItemStack potion = getPotionStack(ammoStack);
        return potion.isEmpty() ? 1 : getPotionAmmoCost(potion.getItem());
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x00FFFF00;
    }

    @Override
    public float getAirUsageMultiplier(Minigun minigun, ItemStack ammoStack) {
        if (minigun.getUpgrades(ModUpgrades.DISPENSER.get()) > 0 && !getPotionStack(ammoStack).isEmpty()) {
            return minigun.getUpgrades(ModUpgrades.DISPENSER.get()) + 1f;
        } else {
            return 1f;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> infoList, TooltipFlag extraInfo) {
        super.appendHoverText(stack, context, infoList, extraInfo);
        ItemStack potion = getPotionStack(stack);
        if (!potion.isEmpty()) {
            List<Component> potionInfo = new ArrayList<>();
            potion.getItem().appendHoverText(potion, context, potionInfo, extraInfo);
            String extra = "";
            if (potion.getItem() instanceof SplashPotionItem) {
                extra = " " + I18n.get("pneumaticcraft.gui.tooltip.gunAmmo.splash");
            } else if (potion.getItem() instanceof LingeringPotionItem) {
                extra = " " + I18n.get("pneumaticcraft.gui.tooltip.gunAmmo.lingering");
            }
            infoList.add(xlate("pneumaticcraft.gui.tooltip.gunAmmo").append(" ").append(potionInfo.get(0)).append(extra));
        } else {
            infoList.add(xlate("pneumaticcraft.gui.tooltip.gunAmmo.combineWithPotion"));
        }
    }

    @Override
    public int onTargetHit(Minigun minigun, ItemStack ammo, Entity target) {
        ItemStack potion = getPotionStack(ammo);
        if (!potion.isEmpty() && target instanceof LivingEntity entity) {
            Player shooter = minigun.getPlayer();
            if (minigun.dispenserWeightedPercentage(ConfigHelper.common().minigun.potionProcChance.get(), 0.25f)) {
                if (potion.getItem() == Items.POTION) {
                    potion.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
                            .forEachEffect(effect -> entity.addEffect(new MobEffectInstance(effect)));
                    entity.level().playSound(null, entity.blockPosition(), SoundEvents.SPLASH_POTION_BREAK, SoundSource.PLAYERS, 1f, 1f);
                } else if (potion.getItem() == Items.SPLASH_POTION || potion.getItem() == Items.LINGERING_POTION) {
                    ThrownPotion entityPotion = new ThrownPotion(shooter.level(), shooter);
                    entityPotion.setItem(potion);
                    entityPotion.setPos(entity.getX(), entity.getY(), entity.getZ());
                    shooter.level().addFreshEntity(entityPotion);
                }
            }
            return getPotionAmmoCost(potion.getItem());
        } else {
            return super.onTargetHit(minigun, ammo, target);
        }
    }

    @Override
    public int onBlockHit(Minigun minigun, ItemStack ammo, BlockHitResult brtr) {
        ItemStack potion = getPotionStack(ammo);
        if (potion.getItem() == Items.SPLASH_POTION || potion.getItem() == Items.LINGERING_POTION) {
            Player shooter = minigun.getPlayer();
            int chance = ConfigHelper.common().minigun.potionProcChance.get() + minigun.getUpgrades(ModUpgrades.DISPENSER.get()) * 2;
            if (shooter.level().random.nextInt(100) < chance) {
                ThrownPotion entityPotion = new ThrownPotion(shooter.level(), shooter);
                entityPotion.setItem(potion);
                BlockPos pos2 = brtr.getBlockPos().relative(brtr.getDirection());
                entityPotion.setPos(pos2.getX() + 0.5, pos2.getY() + 0.5, pos2.getZ() + 0.5);
                shooter.level().addFreshEntity(entityPotion);
            }
            return getPotionAmmoCost(potion.getItem());
        } else {
            return super.onBlockHit(minigun, ammo, brtr);
        }
    }

    private static int getPotionAmmoCost(Item item) {
        if (item == Items.LINGERING_POTION) {
            return 6;
        } else if (item == Items.SPLASH_POTION) {
            return 3;
        } else if (item == Items.POTION) {
            return 1;
        } else {
            throw new IllegalArgumentException("Item " + item + " is not a potion!");
        }
    }
}
