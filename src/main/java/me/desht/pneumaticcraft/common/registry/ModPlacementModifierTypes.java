package me.desht.pneumaticcraft.common.registry;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.worldgen.OilLakeFilter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModPlacementModifierTypes {
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS
            = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, Names.MOD_ID);

    public static final Supplier<PlacementModifierType<OilLakeFilter>> OIL_LAKE_FILTER
            = PLACEMENT_MODIFIERS.register("oil_lake_filter", () -> () -> OilLakeFilter.CODEC);
}
