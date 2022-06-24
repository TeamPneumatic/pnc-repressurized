package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.misc.DamageSources;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class PNCDamageSource extends DamageSource {
    public static final DamageSource PRESSURE = new PNCDamageSource("pressure", 2).bypassArmor();
    public static final DamageSource ETCHING_ACID = new PNCDamageSource("acid", 2);
    public static final DamageSource SECURITY_STATION = new PNCDamageSource("securityStation").bypassArmor();
    public static final DamageSource PLASTIC_BLOCK = new PNCDamageSource("plastic_block", 2);

    private final int deathMessageCount;

    private PNCDamageSource(String damageType, int messages) {
        super(damageType);
        deathMessageCount = messages;
    }

    private PNCDamageSource(String damageType) {
        this(damageType, 1);
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity dyingEntity) {
        int messageNumber = dyingEntity.getRandom().nextInt(deathMessageCount) + 1;

        LivingEntity killer = dyingEntity.getKillCredit();
        String s = PneumaticRegistry.MOD_ID + ".death.attack." + msgId + messageNumber;
        String s1 = s + ".player";
        return killer != null && I18n.exists(s1) ?
                Component.translatable(s1, dyingEntity.getDisplayName(), killer.getDisplayName()) :
                Component.translatable(s, dyingEntity.getDisplayName());
    }

    public static class DamageSourceDroneOverload extends PNCDamageSource {
        private final String msgKey;
        private final Object[] params;

        public DamageSourceDroneOverload(String msgKey, Object... params) {
            super("droneOverload");
            bypassArmor();
            bypassInvul();
            this.msgKey = msgKey;
            this.params = new Object[params.length];
            System.arraycopy(params, 0, this.params, 0, params.length);
        }

        @Override
        public Component getLocalizedDeathMessage(LivingEntity dyingEntity) {
            return Component.translatable("pneumaticcraft.death.drone.overload." + msgKey, params);
        }
    }

    public enum DamageSourcesImpl implements DamageSources {
        INSTANCE;

        @Override
        public boolean isPressureDamage(DamageSource damageSource) {
            return damageSource == PRESSURE;
        }

        @Override
        public boolean isSecurityStationDamage(DamageSource damageSource) {
            return damageSource == SECURITY_STATION;
        }

        @Override
        public boolean isEtchingAcidDamage(DamageSource damageSource) {
            return damageSource == ETCHING_ACID;
        }

        @Override
        public boolean isPlasticBlockDamage(DamageSource damageSource) {
            return damageSource == PLASTIC_BLOCK;
        }

        @Override
        public boolean isDroneOverloadDamage(DamageSource damageSource) {
            return damageSource instanceof DamageSourceDroneOverload;
        }
    }
}
