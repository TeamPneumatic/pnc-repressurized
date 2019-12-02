package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.common.fluid.*;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.fluid.Fluid;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Names.MOD_ID)
public class ModFluids {
    public static final Fluid OIL = null;
    public static final Fluid OIL_FLOWING = null;
    public static final Fluid ETCHING_ACID = null;
    public static final Fluid ETCHING_ACID_FLOWING = null;
    public static final Fluid PLASTIC = null;
    public static final Fluid PLASTIC_FLOWING = null;
    public static final Fluid DIESEL = null;
    public static final Fluid DIESEL_FLOWING = null;
    public static final Fluid KEROSENE = null;
    public static final Fluid KEROSENE_FLOWING = null;
    public static final Fluid GASOLINE = null;
    public static final Fluid GASOLINE_FLOWING = null;
    public static final Fluid LPG = null;
    public static final Fluid LPG_FLOWING = null;
    public static final Fluid LUBRICANT = null;
    public static final Fluid LUBRICANT_FLOWING = null;

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void onEntityRegister(RegistryEvent.Register<Fluid> event) {
            event.getRegistry().registerAll(
                new FluidOil.Source(), new FluidOil.Flowing(),
                new FluidEtchingAcid.Source(), new FluidEtchingAcid.Flowing(),
                new FluidPlastic.Source(), new FluidPlastic.Flowing(),
                new FluidDiesel.Source(), new FluidDiesel.Flowing(),
                new FluidKerosene.Source(), new FluidKerosene.Flowing(),
                new FluidGasoline.Source(), new FluidGasoline.Flowing(),
                new FluidLPG.Source(), new FluidLPG.Flowing(),
                new FluidLubricant.Source(), new FluidLubricant.Flowing()
            );
        }
    }
}
