package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.worldgen.OilLakeFilter;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModPlacementModifierTypes {
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegister.create(Registry.PLACEMENT_MODIFIER_REGISTRY, Names.MOD_ID);

    public static final RegistryObject<PlacementModifierType<OilLakeFilter>> OIL_LAKE_FILTER = PLACEMENT_MODIFIERS.register("oil_lake_filter", () -> () -> OilLakeFilter.CODEC);
}
