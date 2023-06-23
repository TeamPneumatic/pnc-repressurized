package me.desht.pneumaticcraft.api.misc;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public class DamageTypes {
    public static final ResourceKey<DamageType> ETCHING_ACID = ResourceKey.create(Registries.DAMAGE_TYPE, PneumaticRegistry.RL("etching_acid"));
    public static final ResourceKey<DamageType> PRESSURE = ResourceKey.create(Registries.DAMAGE_TYPE, PneumaticRegistry.RL("pressure"));
    public static final ResourceKey<DamageType> PLASTIC_BLOCK = ResourceKey.create(Registries.DAMAGE_TYPE, PneumaticRegistry.RL("plastic_block"));
    public static final ResourceKey<DamageType> SECURITY_STATION = ResourceKey.create(Registries.DAMAGE_TYPE, PneumaticRegistry.RL("security_station"));
    public static final ResourceKey<DamageType> MINIGUN = ResourceKey.create(Registries.DAMAGE_TYPE, PneumaticRegistry.RL("minigun"));
    public static final ResourceKey<DamageType> MINIGUN_AP = ResourceKey.create(Registries.DAMAGE_TYPE, PneumaticRegistry.RL("minigun_ap"));
}
