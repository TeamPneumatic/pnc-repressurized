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

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class ItemGunAmmo extends Item implements ColorHandlers.ITintableItem {

    public ItemGunAmmo() {
        super(ModItems.defaultProps().stacksTo(1).setNoRepair().defaultDurability(1000));
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return PNCConfig.Common.Minigun.standardAmmoCartridgeSize;
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
        return DamageSource.playerAttack(minigun.getPlayer());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, World world, List<ITextComponent> infoList, ITooltipFlag extraInfo) {
        infoList.add(xlate("pneumaticcraft.gui.tooltip.gunAmmo.ammoRemaining", stack.getMaxDamage() - stack.getDamageValue(), stack.getMaxDamage()));
        super.appendHoverText(stack, world, infoList, extraInfo);
    }

    @Override
    public int getTintColor(ItemStack stack, int tintIndex) {
        return tintIndex == 1 ? getAmmoColor(stack) : 0xFFFFFFFF;
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
        int nSpeed = minigun.getUpgrades(EnumUpgrade.SPEED);
        for (int i = 0; i < nSpeed; i++) {
            if (minigun.getWorld().random.nextInt(100) < 20) times++;
        }

        double dmgMult = getDamageMultiplier(target, ammo);
        if (dmgMult > 0) {
            // note: can't just check for PartEntity (will get ClassNotFoundException on dedicated server)
            if (target instanceof LivingEntity || target instanceof EnderDragonPartEntity || target instanceof EnderCrystalEntity) {
                target.hurt(getDamageSource(minigun), (float)(PNCConfig.Common.Minigun.baseDamage * dmgMult * times));
            } else if (target instanceof ShulkerBulletEntity || target instanceof DamagingProjectileEntity) {
                target.remove();
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
    public int onBlockHit(Minigun minigun, ItemStack ammo, BlockRayTraceResult brtr) {
        if (PNCConfig.Common.Minigun.blockHitParticles) {
            World w = minigun.getPlayer().level;
            BlockState state = w.getBlockState(brtr.getBlockPos());
            Direction face = brtr.getDirection();
            Vector3d hitVec = brtr.getLocation();
            IParticleData data = new BlockParticleData(ParticleTypes.BLOCK, state);
            ((ServerWorld) w).sendParticles(data, hitVec.x, hitVec.y, hitVec.z, 10,
                    face.getStepX() * 0.2, face.getStepY() * 0.2, face.getStepZ() * 0.2, 0.05);
        }

        // not taking speed upgrades into account here; being kind to players who miss a lot...
        return 1;
    }
}
