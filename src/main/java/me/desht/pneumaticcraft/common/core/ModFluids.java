package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.common.fluid.*;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.fluid.Fluid;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS = new DeferredRegister<>(ForgeRegistries.FLUIDS, Names.MOD_ID);

    public static final RegistryObject<Fluid> OIL = register("oil", FluidOil.Source::new);
    public static final RegistryObject<Fluid> OIL_FLOWING = register("oil_flowing", FluidOil.Flowing::new);

    public static final RegistryObject<Fluid> ETCHING_ACID = register("etching_acid", FluidEtchingAcid.Source::new);
    public static final RegistryObject<Fluid> ETCHING_ACID_FLOWING = register("etching_acid_flowing", FluidEtchingAcid.Flowing::new);

    public static final RegistryObject<Fluid> PLASTIC = register("plastic", FluidPlastic.Source::new);
    public static final RegistryObject<Fluid> PLASTIC_FLOWING = register("plastic_flowing", FluidPlastic.Flowing::new);

    public static final RegistryObject<Fluid> DIESEL = register("diesel", FluidDiesel.Source::new);
    public static final RegistryObject<Fluid> DIESEL_FLOWING = register("diesel_flowing", FluidDiesel.Flowing::new);

    public static final RegistryObject<Fluid> KEROSENE = register("kerosene", FluidKerosene.Source::new);
    public static final RegistryObject<Fluid> KEROSENE_FLOWING = register("kerosene_flowing", FluidKerosene.Flowing::new);

    public static final RegistryObject<Fluid> GASOLINE = register("gasoline", FluidGasoline.Source::new);
    public static final RegistryObject<Fluid> GASOLINE_FLOWING = register("gasoline_flowing", FluidGasoline.Flowing::new);

    public static final RegistryObject<Fluid> LPG = register("lpg", FluidLPG.Source::new);
    public static final RegistryObject<Fluid> LPG_FLOWING = register("lpg_flowing", FluidLPG.Flowing::new);

    public static final RegistryObject<Fluid> LUBRICANT = register("lubricant", FluidLubricant.Source::new);
    public static final RegistryObject<Fluid> LUBRICANT_FLOWING = register("lubricant_flowing", FluidLubricant.Flowing::new);

    private static <T extends Fluid> RegistryObject<T> register(String name, final Supplier<T> sup) {
        return FLUIDS.register(name, sup);
    }
}
