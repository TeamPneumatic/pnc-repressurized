package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.misc.DamageTypes;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageTypeProvider {
    public static void bootstrap(BootstapContext<DamageType> ctx) {
        ctx.register(DamageTypes.ETCHING_ACID, new DamageType("pnc_acid", 01.f));
        ctx.register(DamageTypes.PRESSURE, new DamageType("pnc_pressure", 01.f));
        ctx.register(DamageTypes.PLASTIC_BLOCK, new DamageType("pnc_plastic_block", 01.f));
        ctx.register(DamageTypes.MINIGUN, new DamageType("pnc_minigun", 01.f));
        ctx.register(DamageTypes.MINIGUN_AP, new DamageType("pnc_minigun", 01.f));
        ctx.register(DamageTypes.SECURITY_STATION, new DamageType("pnc_security_station", 01.f));
    }
}
