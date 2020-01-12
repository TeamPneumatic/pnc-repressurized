package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.config.PNCConfig;
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
        return PNCConfig.Common.Minigun.incendiaryAmmoCartridgeSize;
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x00FF8000;
    }

    @Override
    protected DamageSource getDamageSource(Minigun minigun) {
        return super.getDamageSource(minigun).setFireDamage();
    }

    @Override
    protected float getDamageMultiplier(Entity target, ItemStack ammoStack) {
        double mult = super.getDamageMultiplier(target, ammoStack);
        if (target != null && target.isImmuneToFire()) {
            mult *= 0.1;
        }
        return (float) mult;
    }

    @Override
    public int onTargetHit(Minigun minigun, ItemStack ammo, Entity target) {
        if (minigun.dispenserWeightedPercentage(PNCConfig.Common.Minigun.incendiaryAmmoEntityIgniteChance)) {
            target.setFire(PNCConfig.Common.Minigun.incendiaryAmmoFireDuration);
        }
        return super.onTargetHit(minigun, ammo, target);
    }

    @Override
    public int onBlockHit(Minigun minigun, ItemStack ammo, BlockRayTraceResult brtr) {
        if (minigun.dispenserWeightedPercentage(PNCConfig.Common.Minigun.incendiaryAmmoBlockIgniteChance)) {
            PneumaticCraftUtils.tryPlaceBlock(minigun.getWorld(), brtr.getPos().offset(brtr.getFace()), minigun.getPlayer(), brtr.getFace(), Blocks.FIRE.getDefaultState());
        }
        return super.onBlockHit(minigun, ammo, brtr);
    }
}
