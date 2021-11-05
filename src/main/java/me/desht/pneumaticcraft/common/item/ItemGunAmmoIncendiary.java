package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockRayTraceResult;

public class ItemGunAmmoIncendiary extends ItemGunAmmo {

    @Override
    public int getMaxDamage(ItemStack stack) {
        return ConfigHelper.common().minigun.incendiaryAmmoCartridgeSize.get();
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x00FF8000;
    }

    @Override
    protected DamageSource getDamageSource(Minigun minigun) {
        return super.getDamageSource(minigun).setIsFire();
    }

    @Override
    protected float getDamageMultiplier(Entity target, ItemStack ammoStack) {
        double mult = super.getDamageMultiplier(target, ammoStack);
        if (target != null && target.fireImmune()) {
            mult *= 0.1;
        }
        return (float) mult;
    }

    @Override
    public int onTargetHit(Minigun minigun, ItemStack ammo, Entity target) {
        if (minigun.dispenserWeightedPercentage(ConfigHelper.common().minigun.incendiaryAmmoEntityIgniteChance.get())) {
            target.setSecondsOnFire(ConfigHelper.common().minigun.incendiaryAmmoFireDuration.get());
        }
        return super.onTargetHit(minigun, ammo, target);
    }

    @Override
    public int onBlockHit(Minigun minigun, ItemStack ammo, BlockRayTraceResult brtr) {
        if (minigun.dispenserWeightedPercentage(ConfigHelper.common().minigun.incendiaryAmmoBlockIgniteChance.get())) {
            PneumaticCraftUtils.tryPlaceBlock(minigun.getWorld(), brtr.getBlockPos().relative(brtr.getDirection()), minigun.getPlayer(), brtr.getDirection(), Blocks.FIRE.defaultBlockState());
        }
        return super.onBlockHit(minigun, ammo, brtr);
    }
}
