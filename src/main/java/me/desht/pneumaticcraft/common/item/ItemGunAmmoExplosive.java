package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class ItemGunAmmoExplosive extends ItemGunAmmo {
    public ItemGunAmmoExplosive() {
        super("gun_ammo_explosive");
    }

    @Override
    protected int getCartridgeSize() {
        return ConfigHandler.minigun.explosiveAmmoCartridgeSize;
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x00FF0000;
    }

    @Override
    public float getDamageMultiplier(Entity target, ItemStack ammoStack) {
        return ConfigHandler.minigun.explosiveAmmoDamageMultiplier;
    }

    @Override
    public int onTargetHit(Minigun minigun, ItemStack ammo, Entity target) {
        if (minigun.dispenserWeightedPercentage(ConfigHandler.minigun.explosiveAmmoExplosionChance)) {
            minigun.getWorld().createExplosion(null, target.posX, target.posY, target.posZ,
                    ConfigHandler.minigun.explosiveAmmoExplosionPower, ConfigHandler.minigun.explosiveAmmoTerrainDamage);
        }
        return super.onTargetHit(minigun, ammo, target);
    }

    @Override
    public int onBlockHit(Minigun minigun, ItemStack ammo, BlockPos pos, EnumFacing face, Vec3d hitVec) {
        if (minigun.dispenserWeightedPercentage(ConfigHandler.minigun.explosiveAmmoExplosionChance)) {
            minigun.getWorld().createExplosion(null, hitVec.x, hitVec.y, hitVec.z,
                    ConfigHandler.minigun.explosiveAmmoExplosionPower, ConfigHandler.minigun.explosiveAmmoTerrainDamage);
        }
        return super.onBlockHit(minigun, ammo, pos, face, hitVec);
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> infoList, ITooltipFlag extraInfo) {
        super.addInformation(stack, world, infoList, extraInfo);
        if (ConfigHandler.minigun.explosiveAmmoTerrainDamage) {
            infoList.add(I18n.format("gui.tooltip.terrainWarning"));
        } else {
            infoList.add(I18n.format("gui.tooltip.terrainSafe"));
        }
    }
}
