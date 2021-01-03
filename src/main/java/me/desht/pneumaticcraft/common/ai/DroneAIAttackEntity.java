package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.item.ItemGunAmmo;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

public class DroneAIAttackEntity extends MeleeAttackGoal {
    private final EntityDrone attacker;
    private final boolean isRanged;
    private final double rangedAttackRange;

    public DroneAIAttackEntity(EntityDrone attacker, double speed, boolean useLongMemory) {
        super(attacker, speed, useLongMemory);
        this.attacker = attacker;
        isRanged = attacker.hasMinigun();
        float rangeMult = 1.0f;
        if (isRanged) {
            ItemStack stack = attacker.getMinigun().getAmmoStack();
            if (stack.getItem() instanceof ItemGunAmmo) {
                rangeMult = ((ItemGunAmmo) stack.getItem()).getRangeMultiplier(stack);
            }
        }
        rangedAttackRange = (16 + Math.min(16, attacker.getUpgrades(EnumUpgrade.RANGE))) * rangeMult;
    }

    @Override
    public boolean shouldExecute() {
        if (isRanged && attacker.getSlotForAmmo() < 0) {
            attacker.getDebugger().addEntry("pneumaticcraft.gui.progWidget.entityAttack.debug.noAmmo");
            return false;
        }

        LivingEntity entitylivingbase = attacker.getAttackTarget();
        if (entitylivingbase == null) {
            attacker.getDebugger().addEntry("pneumaticcraft.gui.progWidget.entityAttack.debug.noEntityToAttack");
        }

        return super.shouldExecute();
    }

    @Override
    public void startExecuting() {
        super.startExecuting();

        // switch to the carried melee weapon with the highest attack damage
        if (attacker.getAttackTarget() != null && attacker.getInv().getSlots() > 1) {
            int bestSlot = 0;
            double bestDmg = 0;
            for (int i = 0; i < attacker.getInv().getSlots(); i++) {
                ItemStack stack = attacker.getInv().getStackInSlot(i);
                if (!stack.isEmpty()) {
                    ModifiableAttributeInstance damage = new ModifiableAttributeInstance(Attributes.ATTACK_DAMAGE, c -> {});
                    for (AttributeModifier modifier : stack.getAttributeModifiers(EquipmentSlotType.MAINHAND).get(Attributes.ATTACK_DAMAGE)) {
                        damage.applyNonPersistentModifier(modifier);
                    }
                    float f1 = EnchantmentHelper.getModifierForCreature(stack, attacker.getAttackTarget().getCreatureAttribute());
                    if (damage.getValue() + f1 > bestDmg) {
                        bestDmg = damage.getValue() + f1;
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
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (isRanged) {
            LivingEntity entitylivingbase = attacker.getAttackTarget();
            if (entitylivingbase == null) return false;
            double dist = attacker.getDistanceSq(entitylivingbase.getPosX(), entitylivingbase.getBoundingBox().minY, entitylivingbase.getPosZ());
            if (attacker.getSlotForAmmo() < 0) return false;
            if (dist < Math.pow(rangedAttackRange, 2) && attacker.getEntitySenses().canSee(entitylivingbase))
                return true;
        }
        return super.shouldContinueExecuting();
    }

    @Override
    public void resetTask() {

    }

    @Override
    public void tick() {
        boolean needingSuper = true;
        if (isRanged) {
            LivingEntity entitylivingbase = attacker.getAttackTarget();
            double dist = attacker.getDistanceSq(entitylivingbase.getPosX(), entitylivingbase.getBoundingBox().minY, entitylivingbase.getPosZ());
            if (dist < Math.pow(rangedAttackRange, 2) && attacker.getEntitySenses().canSee(entitylivingbase)) {
                attacker.getFakePlayer().setPosition(attacker.getPosX(), attacker.getPosY(), attacker.getPosZ());
                attacker.tryFireMinigun(entitylivingbase);
                needingSuper = false;
                if (dist < Math.pow(rangedAttackRange * 0.75, 2)) {
                    attacker.getNavigator().clearPath();
                }
            }
        }
        if (needingSuper) super.tick();
    }
}
