/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.fluid.*;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, Names.MOD_ID);

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

    public static final RegistryObject<Fluid> MEMORY_ESSENCE = register("memory_essence", FluidMemoryEssence.Source::new);
    public static final RegistryObject<Fluid> MEMORY_ESSENCE_FLOWING = register("memory_essence_flowing", FluidMemoryEssence.Flowing::new);

    public static final RegistryObject<Fluid> YEAST_CULTURE = register("yeast_culture", FluidYeastCulture.Source::new);
    public static final RegistryObject<Fluid> YEAST_CULTURE_FLOWING = register("yeast_culture_flowing", FluidYeastCulture.Flowing::new);

    public static final RegistryObject<Fluid> ETHANOL = register("ethanol", FluidEthanol.Source::new);
    public static final RegistryObject<Fluid> ETHANOL_FLOWING = register("ethanol_flowing", FluidEthanol.Flowing::new);

    public static final RegistryObject<Fluid> VEGETABLE_OIL = register("vegetable_oil", FluidVegetableOil.Source::new);
    public static final RegistryObject<Fluid> VEGETABLE_OIL_FLOWING = register("vegetable_oil_flowing", FluidVegetableOil.Flowing::new);

    public static final RegistryObject<Fluid> BIODIESEL = register("biodiesel", FluidBiodiesel.Source::new);
    public static final RegistryObject<Fluid> BIODIESEL_FLOWING = register("biodiesel_flowing", FluidBiodiesel.Flowing::new);

    private static <T extends Fluid> RegistryObject<T> register(String name, final Supplier<T> sup) {
        return FLUIDS.register(name, sup);
    }
}
