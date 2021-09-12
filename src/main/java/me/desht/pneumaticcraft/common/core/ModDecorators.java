package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.worldgen.LakeOil;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModDecorators {
    public static final DeferredRegister<Placement<?>> DECORATORS = DeferredRegister.create(ForgeRegistries.DECORATORS, Names.MOD_ID);

    public static final RegistryObject<LakeOil> OIL_LAKE = register("oil_lake", () -> new LakeOil(ChanceConfig.CODEC));

    private static <T extends Placement<?>> RegistryObject<T> register(final String name, final Supplier<T> sup) {
        return DECORATORS.register(name, sup);
    }
}
