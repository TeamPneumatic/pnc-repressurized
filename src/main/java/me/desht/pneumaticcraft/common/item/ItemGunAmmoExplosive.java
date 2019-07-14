package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.config.Config;
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
    public ItemGunAmmoExplosive() {
        super(DEFAULT_PROPS.maxDamage(Config.Common.Minigun.explosiveAmmoCartridgeSize),"gun_ammo_explosive");
    }

    @Override
    public int getAmmoColor(ItemStack ammo) {
        return 0x00FF0000;
    }

    @Override
    public float getDamageMultiplier(Entity target, ItemStack ammoStack) {
        return (float) Config.Common.Minigun.explosiveAmmoDamageMultiplier;
    }

    @Override
    public int onTargetHit(Minigun minigun, ItemStack ammo, Entity target) {
        if (minigun.dispenserWeightedPercentage(Config.Common.Minigun.explosiveAmmoExplosionChance)) {
            Explosion.Mode mode = Config.Common.Minigun.explosiveAmmoTerrainDamage ? Explosion.Mode.BREAK : Explosion.Mode.NONE;
            minigun.getWorld().createExplosion(null, target.posX, target.posY, target.posZ,
                    (float) Config.Common.Minigun.explosiveAmmoExplosionPower, mode);
        }
        return super.onTargetHit(minigun, ammo, target);
    }

    @Override
    public int onBlockHit(Minigun minigun, ItemStack ammo, BlockRayTraceResult brtr) {
        if (minigun.dispenserWeightedPercentage(Config.Common.Minigun.explosiveAmmoExplosionChance)) {
            Explosion.Mode mode = Config.Common.Minigun.explosiveAmmoTerrainDamage ? Explosion.Mode.BREAK : Explosion.Mode.NONE;
            minigun.getWorld().createExplosion(null, brtr.getHitVec().x, brtr.getHitVec().y, brtr.getHitVec().z,
                    (float) Config.Common.Minigun.explosiveAmmoExplosionPower, mode);
        }
        return super.onBlockHit(minigun, ammo, brtr);
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<ITextComponent> infoList, ITooltipFlag extraInfo) {
        super.addInformation(stack, world, infoList, extraInfo);
        if (Config.Common.Minigun.explosiveAmmoTerrainDamage) {
            infoList.add(xlate("gui.tooltip.terrainWarning"));
        } else {
            infoList.add(xlate("gui.tooltip.terrainSafe"));
        }
    }
}
