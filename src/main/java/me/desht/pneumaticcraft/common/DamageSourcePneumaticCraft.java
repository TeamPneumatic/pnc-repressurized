package me.desht.pneumaticcraft.common;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class DamageSourcePneumaticCraft extends DamageSource {
    public static final DamageSourcePneumaticCraft PRESSURE = (DamageSourcePneumaticCraft) new DamageSourcePneumaticCraft("pressure", 2).setDamageBypassesArmor();
    public static final DamageSourcePneumaticCraft ETCHING_ACID = new DamageSourcePneumaticCraft("acid", 2);
    public static final DamageSourcePneumaticCraft SECURITY_STATION = (DamageSourcePneumaticCraft) new DamageSourcePneumaticCraft("securityStation").setDamageBypassesArmor();

    private int deathMessages = 0;

    public DamageSourcePneumaticCraft(String damageType, int messages) {
        this(damageType);
        deathMessages = messages;
    }

    public DamageSourcePneumaticCraft(String damageType) {
        super(damageType);
        deathMessages = 1;
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
    public ITextComponent getDeathMessage(EntityLivingBase par1EntityLivingBase) {
        String messageMeta = "";
        int messageNumber = par1EntityLivingBase.getRNG().nextInt(deathMessages) + 1;
        messageMeta = messageNumber + "";

        EntityLivingBase entitylivingbase1 = par1EntityLivingBase.getAttackingEntity();
        String s = "death.attack." + damageType + messageMeta;
        String s1 = s + ".player";
        return entitylivingbase1 != null && I18n.hasKey(s1) ?
                new TextComponentTranslation(s1, par1EntityLivingBase.getDisplayName(), entitylivingbase1.getDisplayName()) :
                new TextComponentTranslation(s, par1EntityLivingBase.getDisplayName());
    }
}
