package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ItemGunAmmoIncendiary extends ItemGunAmmo {
    public ItemGunAmmoIncendiary() {
        super("gun_ammo_incendiary");
    }

    @Override
    protected int getCartridgeSize() {
        return ConfigHandler.minigun.incendiaryAmmoCartridgeSize;
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
        float mult = super.getDamageMultiplier(target, ammoStack);
        if (target != null && target.isImmuneToFire()) {
            mult *= 0.1;
        }
        return mult;
    }

    @Override
    public int onTargetHit(Minigun minigun, ItemStack ammo, Entity target) {
        if (minigun.dispenserWeightedPercentage(ConfigHandler.minigun.incendiaryAmmoEntityIgniteChance)) {
            target.setFire(ConfigHandler.minigun.incendiaryAmmoFireDuration);
        }
        return super.onTargetHit(minigun, ammo, target);
    }

    @Override
    public int onBlockHit(Minigun minigun, ItemStack ammo, BlockPos pos, EnumFacing face, Vec3d hitVec) {
        if (minigun.dispenserWeightedPercentage(ConfigHandler.minigun.incendiaryAmmoBlockIgniteChance)) {
            PneumaticCraftUtils.tryPlaceBlock(minigun.getWorld(), pos.offset(face), minigun.getPlayer(), face, Blocks.FIRE.getDefaultState());
        }
        return super.onBlockHit(minigun, ammo, pos, face, hitVec);
    }
}
