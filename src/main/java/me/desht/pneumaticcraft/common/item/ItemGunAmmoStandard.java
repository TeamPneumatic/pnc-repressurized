package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemGunAmmoStandard extends ItemGunAmmo {
    private static final String NBT_POTION = "potion";

    @Override
    public int getMaxDamage(ItemStack stack) {
        return ConfigHelper.common().minigun.standardAmmoCartridgeSize.get();
    }

    @Nonnull
    private static ItemStack getPotion(ItemStack ammo) {
        if (ammo.getTag() != null && ammo.getTag().contains(NBT_POTION)) {
            return ItemStack.of(ammo.getTag().getCompound(NBT_POTION));
        } else {
            return ItemStack.EMPTY;
        }
    }

    public static void setPotion(ItemStack ammo, ItemStack potion) {
        CompoundNBT tag = new CompoundNBT();
        potion.save(tag);
        NBTUtils.setCompoundTag(ammo, "potion", tag);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(NBT_POTION);
    }

    @Override
    public int getAmmoCost(ItemStack ammoStack) {
        ItemStack potion = getPotion(ammoStack);
        return potion.isEmpty() ? 1 : getPotionAmmoCost(potion.getItem());
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x00FFFF00;
//        ItemStack potion = getPotion(ammo);
//        return potion.isEmpty() ? 0x00FFFF00 : Minecraft.getInstance().getItemColors().getColor(potion, 0);
    }

    @Override
    public float getAirUsageMultiplier(Minigun minigun, ItemStack ammoStack) {
        if (minigun.getUpgrades(EnumUpgrade.DISPENSER) > 0 && !getPotion(ammoStack).isEmpty()) {
            return minigun.getUpgrades(EnumUpgrade.DISPENSER) + 1f;
        } else {
            return 1f;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, World world, List<ITextComponent> infoList, ITooltipFlag extraInfo) {
        super.appendHoverText(stack, world, infoList, extraInfo);
        ItemStack potion = getPotion(stack);
        if (!potion.isEmpty()) {
            List<ITextComponent> potionInfo = new ArrayList<>();
            potion.getItem().appendHoverText(potion, world, potionInfo, extraInfo);
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
        ItemStack potion = getPotion(ammo);
        if (!potion.isEmpty() && target instanceof LivingEntity) {
            LivingEntity entity = (LivingEntity) target;
            PlayerEntity shooter = minigun.getPlayer();
            if (minigun.dispenserWeightedPercentage(ConfigHelper.common().minigun.potionProcChance.get(), 0.25f)) {
                if (potion.getItem() == Items.POTION) {
                    List<EffectInstance> effects = PotionUtils.getMobEffects(potion);
                    for (EffectInstance effect : effects) {
                        entity.addEffect(new EffectInstance(effect));
                    }
                    entity.level.playSound(null, entity.blockPosition(), SoundEvents.SPLASH_POTION_BREAK, SoundCategory.PLAYERS, 1f, 1f);
                } else if (potion.getItem() == Items.SPLASH_POTION || potion.getItem() == Items.LINGERING_POTION) {
                    PotionEntity entityPotion = new PotionEntity(shooter.level, shooter);
                    entityPotion.setItem(potion);
                    entityPotion.setPos(entity.getX(), entity.getY(), entity.getZ());
                    shooter.level.addFreshEntity(entityPotion);
                }
            }
            return getPotionAmmoCost(potion.getItem());
        } else {
            return super.onTargetHit(minigun, ammo, target);
        }
    }

    @Override
    public int onBlockHit(Minigun minigun, ItemStack ammo, BlockRayTraceResult brtr) {
        ItemStack potion = getPotion(ammo);
        if (potion.getItem() == Items.SPLASH_POTION || potion.getItem() == Items.LINGERING_POTION) {
            PlayerEntity shooter = minigun.getPlayer();
            int chance = ConfigHelper.common().minigun.potionProcChance.get() + minigun.getUpgrades(EnumUpgrade.DISPENSER) * 2;
            if (shooter.level.random.nextInt(100) < chance) {
                PotionEntity entityPotion = new PotionEntity(shooter.level, shooter);
                entityPotion.setItem(potion);
                BlockPos pos2 = brtr.getBlockPos().relative(brtr.getDirection());
                entityPotion.setPos(pos2.getX() + 0.5, pos2.getY() + 0.5, pos2.getZ() + 0.5);
                shooter.level.addFreshEntity(entityPotion);
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
