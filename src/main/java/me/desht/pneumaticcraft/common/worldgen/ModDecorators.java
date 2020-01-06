package me.desht.pneumaticcraft.common.worldgen;

import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.world.gen.placement.LakeChanceConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModDecorators {
    public static final DeferredRegister<Placement<?>> DECORATORS = new DeferredRegister<>(ForgeRegistries.DECORATORS, Names.MOD_ID);

    static final RegistryObject<LakeOil> OIL_LAKE = register("oil_lake", () -> new LakeOil(LakeChanceConfig::deserialize));

    private static <T extends Placement<?>> RegistryObject<T> register(final String name, final Supplier<T> sup) {
        return DECORATORS.register(name, sup);
    }
}
