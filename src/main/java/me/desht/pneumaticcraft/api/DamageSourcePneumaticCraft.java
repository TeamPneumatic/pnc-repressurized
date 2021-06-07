package me.desht.pneumaticcraft.api;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class DamageSourcePneumaticCraft extends DamageSource {
    public static final DamageSource PRESSURE = new DamageSourcePneumaticCraft("pressure", 2).setDamageBypassesArmor();
    public static final DamageSource ETCHING_ACID = new DamageSourcePneumaticCraft("acid", 2);
    public static final DamageSource SECURITY_STATION = new DamageSourcePneumaticCraft("securityStation").setDamageBypassesArmor();
    public static final DamageSource FREEZING = new DamageSourcePneumaticCraft("freezing", 2);
    public static final DamageSource PLASTIC_BLOCK = new DamageSourcePneumaticCraft("plastic_block", 2);

    public static boolean isDroneOverload(DamageSource src) {
        return src instanceof DamageSourceDroneOverload;
    }

    private final int deathMessageCount;

    private DamageSourcePneumaticCraft(String damageType, int messages) {
        super(damageType);
        deathMessageCount = messages;
    }

    DamageSourcePneumaticCraft(String damageType) {
        this(damageType, 1);
    }

    @Override
    public ITextComponent getDeathMessage(LivingEntity dyingEntity) {
        int messageNumber = dyingEntity.getRNG().nextInt(deathMessageCount) + 1;

        LivingEntity killer = dyingEntity.getAttackingEntity();
        String s = PneumaticRegistry.MOD_ID + ".death.attack." + damageType + messageNumber;
        String s1 = s + ".player";
        return killer != null && I18n.hasKey(s1) ?
                new TranslationTextComponent(s1, dyingEntity.getDisplayName(), killer.getDisplayName()) :
                new TranslationTextComponent(s, dyingEntity.getDisplayName());
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
        public ITextComponent getDeathMessage(LivingEntity dyingEntity) {
            return new TranslationTextComponent("pneumaticcraft.death.drone.overload." + msgKey, params);
        }
    }
}
