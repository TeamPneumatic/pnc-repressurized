package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.misc.DamageSources;
import me.desht.pneumaticcraft.api.misc.DamageTypes;
import me.desht.pneumaticcraft.mixin.accessors.DamageSourcesAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class PNCDamageSource {
    public static DamageSource pressure(Level level) {
        return source(level, DamageTypes.PRESSURE);
    }

    public static DamageSource acid(Level level) {
        return source(level, DamageTypes.ETCHING_ACID);
    }

    public static DamageSource plasticBlock(Level level) {
        return source(level, DamageTypes.PLASTIC_BLOCK);
    }

    public static DamageSource securityStation(Level level) {
        return source(level, DamageTypes.SECURITY_STATION);
    }

    public static DamageSource minigun(Level level, Entity cause) {
        return source(level, DamageTypes.MINIGUN, cause, cause);
    }

    public static DamageSource minigunAP(Level level, Entity cause) {
        return source(level, DamageTypes.MINIGUN_AP, cause, cause);
    }

    private static DamageSource source(Level level, ResourceKey<DamageType>type) {
        return source(level, type, null, null);
    }

    private static DamageSource source(Level level, ResourceKey<DamageType>type, Entity cause, Entity direct) {
        return ((DamageSourcesAccess) level.damageSources()).invokeSource(type, cause, direct);
    }

    public enum DamageSourcesImpl implements DamageSources {
        INSTANCE;

        @Override
        public boolean isPressureDamage(DamageSource damageSource) {
            return damageSource.is(PneumaticCraftTags.DamageTypes.PRESSURE);
        }

        @Override
        public boolean isSecurityStationDamage(DamageSource damageSource) {
            return damageSource.is(PneumaticCraftTags.DamageTypes.SECURITY_STATION);
        }

        @Override
        public boolean isEtchingAcidDamage(DamageSource damageSource) {
            return damageSource.is(PneumaticCraftTags.DamageTypes.ACID);
        }

        @Override
        public boolean isPlasticBlockDamage(DamageSource damageSource) {
            return damageSource.is(PneumaticCraftTags.DamageTypes.PLASTIC_BLOCK);
        }

        @Override
        public boolean isMinigunDamage(DamageSource damageSource) {
            return damageSource.is(PneumaticCraftTags.DamageTypes.MINIGUN);
        }
    }
}
