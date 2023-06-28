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

import me.desht.pneumaticcraft.common.PNCDamageSource;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class AbstractGunAmmoItem extends Item {

    public AbstractGunAmmoItem() {
        super(ModItems.defaultProps().stacksTo(1).setNoRepair().defaultDurability(1000));
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return ConfigHelper.common().minigun.standardAmmoCartridgeSize.get();
    }

    /**
     * Get this ammo's range modifier.
     *
     * @param ammoStack this ammo
     * @return the range modifier; base minigun range is multiplied by this value
     */
    public float getRangeMultiplier(ItemStack ammoStack) {
        return 1.0f;
    }

    /**
     * Get the air usage multiplier.
     *
     *
     * @param minigun the minigun being used
     * @param ammoStack this ammo
     * @return the usage multiplier; base minigun air usage is multiplied by this value
     */
    public float getAirUsageMultiplier(Minigun minigun, ItemStack ammoStack) {
        return 1.0f;
    }

    /**
     * Get the damage multiplier.
     *
     *
     * @param target the current target
     * @param ammoStack this ammo
     * @return the damage multiplier; standard physical minigun bullet damage is multiplied by this value
     */
    protected float getDamageMultiplier(Entity target, ItemStack ammoStack) {
        return 1.0f;
    }

    /**
     * Get the color used to render this ammo, both when rendering the minigun model, and when drawing the bullet
     * traces.
     *
     * @param ammo the ammo cartridge
     * @return a rendering color (ARGB format)
     */
    public abstract int getAmmoColor(ItemStack ammo);

    /**
     * Get the cost to fire this ammo, which is the number of rounds used up in one shot.
     *
     * @param ammoStack the ammo stack
     * @return the ammo cost
     */
    public int getAmmoCost(ItemStack ammoStack) {
        return 1;
    }

    protected DamageSource getDamageSource(Minigun minigun) {
        return PNCDamageSource.minigunAP(minigun.getPlayer().level(), minigun.getPlayer());
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> infoList, TooltipFlag extraInfo) {
        infoList.add(xlate("pneumaticcraft.gui.tooltip.gunAmmo.ammoRemaining", stack.getMaxDamage() - stack.getDamageValue(), stack.getMaxDamage()));
        super.appendHoverText(stack, world, infoList, extraInfo);
    }

    /**
     * Called when an entity is shot by the minigun's wielder. This method is responsible for applying any damage and
     * other possible effects to the entity.
     *
     * @param minigun the minigun being used
     * @param ammo the ammo cartridge stack used
     * @param target the targeted entity
     * @return the number of rounds fired
     */
    public int onTargetHit(Minigun minigun, ItemStack ammo, Entity target) {
        int times = 1;
        int nSpeed = minigun.getUpgrades(ModUpgrades.SPEED.get());
        for (int i = 0; i < nSpeed; i++) {
            if (minigun.getWorld().random.nextInt(100) < 20) times++;
        }

        double dmgMult = getDamageMultiplier(target, ammo);
        if (dmgMult > 0) {
            // note: can't just check for PartEntity (will get ClassNotFoundException on dedicated server)
            if (target instanceof LivingEntity || target instanceof EnderDragonPart || target instanceof EndCrystal) {
                target.hurt(getDamageSource(minigun), (float)(ConfigHelper.common().minigun.baseDamage.get() * dmgMult * times));
            } else if (target instanceof ShulkerBullet || target instanceof AbstractHurtingProjectile) {
                target.discard();
            }
        }
        return times;
    }

    /**
     * Called when a block is shot by the minigun's wielder.
     *
     * @param minigun the minigun being used
     * @param ammo the ammo cartridge stack used
     * @param brtr the block raytrace result
     * @return the number of rounds fired
     */
    public int onBlockHit(Minigun minigun, ItemStack ammo, BlockHitResult brtr) {
        // not taking speed upgrades into account here; being kind to players who miss a lot...
        return 1;
    }
}
