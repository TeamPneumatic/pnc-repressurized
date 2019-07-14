package me.desht.pneumaticcraft.common;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class DamageSourcePneumaticCraft extends DamageSource {
    public static final DamageSourcePneumaticCraft PRESSURE = (DamageSourcePneumaticCraft) new DamageSourcePneumaticCraft("pressure", 2).setDamageBypassesArmor();
    public static final DamageSourcePneumaticCraft ETCHING_ACID = new DamageSourcePneumaticCraft("acid", 2);
    public static final DamageSourcePneumaticCraft SECURITY_STATION = (DamageSourcePneumaticCraft) new DamageSourcePneumaticCraft("securityStation").setDamageBypassesArmor();
    public static final DamageSourcePneumaticCraft FREEZING = new DamageSourcePneumaticCraft("freezing", 2);

    private final int deathMessages;

    private DamageSourcePneumaticCraft(String damageType, int messages) {
        super(damageType);
        deathMessages = messages;
    }

    DamageSourcePneumaticCraft(String damageType) {
        this(damageType, 1);
    }

    @Override
    public DamageSource setDamageBypassesArmor() {
        return super.setDamageBypassesArmor();
    }

    @Override
    public DamageSource setDamageAllowedInCreativeMode() {
        return super.setDamageAllowedInCreativeMode();
    }

    @Override
    public DamageSource setFireDamage() {
        return super.setFireDamage();
    }

    /**
     * Returns the message to be displayed on player death.
     */
    @Override
    public ITextComponent getDeathMessage(LivingEntity par1EntityLivingBase) {
        int messageNumber = par1EntityLivingBase.getRNG().nextInt(deathMessages) + 1;

        LivingEntity entitylivingbase1 = par1EntityLivingBase.getAttackingEntity();
        String s = "death.attack." + damageType + messageNumber;
        String s1 = s + ".player";
        return entitylivingbase1 != null && I18n.hasKey(s1) ?
                new TranslationTextComponent(s1, par1EntityLivingBase.getDisplayName(), entitylivingbase1.getDisplayName()) :
                new TranslationTextComponent(s, par1EntityLivingBase.getDisplayName());
    }

    public static class DamageSourceDroneOverload extends DamageSourcePneumaticCraft {
        private final String msgKey;
        private final Object[] params;

        public DamageSourceDroneOverload(String msgKey, Object... params) {
            super("droneOverload");
            setDamageBypassesArmor();
            setDamageAllowedInCreativeMode();
            this.msgKey = msgKey;
            this.params = new Object[params.length];
            System.arraycopy(params, 0, this.params, 0, params.length);
        }

        @Override
        public ITextComponent getDeathMessage(LivingEntity par1EntityLivingBase) {
            return new TranslationTextComponent("death.drone.overload." + msgKey, params);
        }
    }
}
