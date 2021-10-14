package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.worldgen.OilLakeFeature;
import net.minecraft.world.gen.feature.BlockStateFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, Names.MOD_ID);

    public static final RegistryObject<Feature<BlockStateFeatureConfig>> OIL_LAKE = register("oil_lake", OilLakeFeature::new);

    private static <T extends Feature<?>> RegistryObject<T> register(String name, final Supplier<T> sup) {
        return FEATURES.register(name, sup);
    }
}
