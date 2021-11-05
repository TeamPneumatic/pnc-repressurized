package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemGunAmmoExplosive extends ItemGunAmmo {
    @Override
    public int getMaxDamage(ItemStack stack) {
        return ConfigHelper.common().minigun.explosiveAmmoCartridgeSize.get();
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x00FF0000;
    }

    @Override
    public float getDamageMultiplier(Entity target, ItemStack ammoStack) {
        return ConfigHelper.common().minigun.explosiveAmmoDamageMultiplier.get().floatValue();
    }

    @Override
    public int onTargetHit(Minigun minigun, ItemStack ammo, Entity target) {
        if (minigun.dispenserWeightedPercentage(ConfigHelper.common().minigun.explosiveAmmoExplosionChance.get())) {
            Explosion.Mode mode = ConfigHelper.common().minigun.explosiveAmmoTerrainDamage.get() ? Explosion.Mode.BREAK : Explosion.Mode.NONE;
            minigun.getWorld().explode(null, target.getX(), target.getY(), target.getZ(),
                    ConfigHelper.common().minigun.explosiveAmmoExplosionPower.get().floatValue(), mode);
        }
        return super.onTargetHit(minigun, ammo, target);
    }

    @Override
    public int onBlockHit(Minigun minigun, ItemStack ammo, BlockRayTraceResult brtr) {
        if (minigun.dispenserWeightedPercentage(ConfigHelper.common().minigun.explosiveAmmoExplosionChance.get())) {
            Explosion.Mode mode = ConfigHelper.common().minigun.explosiveAmmoTerrainDamage.get() ? Explosion.Mode.BREAK : Explosion.Mode.NONE;
            minigun.getWorld().explode(null, brtr.getLocation().x, brtr.getLocation().y, brtr.getLocation().z,
                    ConfigHelper.common().minigun.explosiveAmmoExplosionPower.get().floatValue(), mode);
        }
        return super.onBlockHit(minigun, ammo, brtr);
    }

    @Override
    public void appendHoverText(ItemStack stack, World world, List<ITextComponent> infoList, ITooltipFlag extraInfo) {
        super.appendHoverText(stack, world, infoList, extraInfo);
        if (ConfigHelper.common().minigun.explosiveAmmoTerrainDamage.get()) {
            infoList.add(xlate("pneumaticcraft.gui.tooltip.terrainWarning"));
        } else {
            infoList.add(xlate("pneumaticcraft.gui.tooltip.terrainSafe"));
        }
    }
}
