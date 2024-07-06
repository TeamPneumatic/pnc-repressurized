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

package me.desht.pneumaticcraft.common.drone.ai;

import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.item.MicromissilesItem;
import me.desht.pneumaticcraft.common.item.minigun.AbstractGunAmmoItem;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.Objects;

public class DroneAIAttackEntity extends MeleeAttackGoal {
    private final DroneEntity attacker;
    private final AttackType attackType;
    private final double rangedAttackRange;

    public DroneAIAttackEntity(DroneEntity attacker, double speed, boolean useLongMemory, String filterString) {
        super(attacker, speed, useLongMemory);
        this.attacker = attacker;
        this.attackType = AttackType.forDrone(attacker);
        float rangeMult = 1.0f;
        switch (attackType) {
            case MINIGUN -> {
                ItemStack stack = attacker.getMinigun().getAmmoStack();
                if (stack.getItem() instanceof AbstractGunAmmoItem ammo) {
                    rangeMult = ammo.getRangeMultiplier(stack);
                }
            }
            case MISSILE -> {
                MicromissilesItem.setEntityFilter(attacker.getInv().getStackInSlot(0), filterString);
                rangeMult = 2f;
            }
        }
        rangedAttackRange = (16 + Math.min(16, attacker.getUpgrades(ModUpgrades.RANGE.get()))) * rangeMult;
    }

    @Override
    public boolean canUse() {
        switch (attackType) {
            case MINIGUN -> {
                if (attacker.getSlotForAmmo() < 0) {
                    attacker.getDebugger().addEntry("pneumaticcraft.gui.progWidget.entityAttack.debug.noAmmo");
                    return false;
                }
            }
            case MISSILE -> {
                if (!isMissileUsable(attacker)) {
                    attacker.getDebugger().addEntry("pneumaticcraft.gui.progWidget.entityAttack.debug.noMissile");
                    return false;
                }
            }
        }

        LivingEntity target = attacker.getTarget();
        if (target == null || !target.isAlive()) {
            attacker.getDebugger().addEntry("pneumaticcraft.gui.progWidget.entityAttack.debug.noEntityToAttack");
            return false;
        }

        return super.canUse();
    }

    @Override
    public void start() {
        super.start();

        attacker.incAttackCount();

        if (attackType == AttackType.MELEE && attacker.getTarget() != null && attacker.getInv().getSlots() > 1) {
            equipBestMeleeWeapon();
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (attackType != AttackType.MELEE) {
            LivingEntity target = attacker.getTarget();
            if (target == null || !target.isAlive()
                    || attackType == AttackType.MINIGUN && attacker.getSlotForAmmo() < 0
                    || attackType == AttackType.MISSILE && !isMissileUsable(attacker)) {
                return false;
            }
            double dist = attacker.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ());
            if (dist < Math.pow(rangedAttackRange, 2) && attacker.getSensing().hasLineOfSight(target))
                return true;
        }
        return super.canContinueToUse();
    }

    @Override
    public void tick() {
        if (attackType != AttackType.MELEE) {
            LivingEntity target = attacker.getTarget();
            if (target != null) {
                double dist = attacker.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ());
                if (dist < Math.pow(rangedAttackRange, 2) && attacker.getSensing().hasLineOfSight(target)) {
                    attacker.getFakePlayer().setPos(attacker.getX(), attacker.getY(), attacker.getZ());
                    attackType.doAttack(attacker, target);
                    if (dist < Math.pow(rangedAttackRange * 0.75, 2)) {
                        attacker.getNavigation().stop();
                    }
                }
            }
        } else {
            super.tick();
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isMissileUsable(DroneEntity drone) {
        ItemStack stack = drone.getInv().getStackInSlot(0);
        return stack.getItem() instanceof MicromissilesItem && stack.getDamageValue() < stack.getMaxDamage();
    }

    private void equipBestMeleeWeapon() {
        // switch to the carried melee weapon with the highest attack damage
        int bestSlot = 0;
        double bestDmg = 0;
        for (int i = 0; i < attacker.getInv().getSlots(); i++) {
            ItemStack stack = attacker.getInv().getStackInSlot(i);
            if (!stack.isEmpty()) {
                AttributeInstance damage = new AttributeInstance(Attributes.ATTACK_DAMAGE, c -> {});
                stack.getAttributeModifiers().forEach(EquipmentSlot.MAINHAND, (attr, mod) -> {
                    if (attr.is(Attributes.ATTACK_DAMAGE)) {
                        damage.addTransientModifier(mod);
                    }
                });
//                for (AttributeModifier modifier : stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE)) {
//                    damage.addTransientModifier(modifier);
//                }
                float modified = EnchantmentHelper.modifyDamage((ServerLevel) attacker.level(), stack, attacker.getTarget(), attacker.damageSources().mobAttack(attacker), (float) damage.getValue());
                if (modified > bestDmg) {
                    bestDmg = modified;
                    bestSlot = i;
                }
            }
        }
        if (bestSlot != 0) {
            ItemStack copy = attacker.getInv().getStackInSlot(0).copy();
            attacker.getInv().setStackInSlot(0, attacker.getInv().getStackInSlot(bestSlot));
            attacker.getInv().setStackInSlot(bestSlot, copy);
        }
    }

    private enum AttackType {
        MELEE,
        MINIGUN,
        MISSILE;

        static AttackType forDrone(DroneEntity drone) {
            if (drone.hasMinigun()) return MINIGUN;
            if (drone.getInv().getStackInSlot(0).getItem() instanceof MicromissilesItem) return MISSILE;
            return MELEE;
        }

        public void doAttack(DroneEntity attacker, LivingEntity target) {
            switch (this) {
                case MINIGUN -> attacker.tryFireMinigun(target);
                case MISSILE -> {
                    FakePlayer fakePlayer = attacker.getFakePlayer();
                    fakePlayer.lookAt(EntityAnchorArgument.Anchor.EYES, target.position());
                    ItemStack stack = attacker.getInv().getStackInSlot(0);
                    if (stack.getItem() instanceof MicromissilesItem) {
                        fakePlayer.gameMode.useItem(fakePlayer, attacker.level(), stack, InteractionHand.MAIN_HAND);
                    }
                }
            }
        }
    }
}
