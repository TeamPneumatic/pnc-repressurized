package me.desht.pneumaticcraft.common.fluid;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.block.BlockFluidEtchingAcid;
import me.desht.pneumaticcraft.common.block.BlockFluidPneumaticCraft;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class Fluids {
    public static final Set<Fluid> FLUIDS = new HashSet<>();
    public static final Set<IFluidBlock> MOD_FLUID_BLOCKS = new HashSet<>();

    public static final Fluid ETCHING_ACID = createFluid("etchacid",
            fluid -> ((FluidPneumaticCraft)fluid).setCustomColor(0xFFA05C00), BlockFluidEtchingAcid::new);
    public static final Fluid PLASTIC = createFluid("plastic",
            fluid -> {}, fluid -> new BlockFluidPneumaticCraft(fluid, new MaterialLiquid(MapColor.GRAY)));
    public static final Fluid OIL = createFluid("oil",
            fluid -> fluid.setDensity(800).setViscosity(10000), BlockFluidPneumaticCraft::new);
    public static final Fluid LPG = createFluid("lpg",
            fluid -> {}, BlockFluidPneumaticCraft::new);
    public static final Fluid GASOLINE = createFluid("fuel",
            fluid -> {}, BlockFluidPneumaticCraft::new);
    public static final Fluid KEROSENE = createFluid("kerosene",
            fluid -> {}, BlockFluidPneumaticCraft::new);
    public static final Fluid DIESEL = createFluid("diesel",
            fluid -> {}, BlockFluidPneumaticCraft::new);
    public static final Fluid LUBRICANT = createFluid("lubricant",
            fluid -> {}, BlockFluidPneumaticCraft::new);

    private static final Map<String, Block> fluidToBlockMap = new HashMap<>(); //you could theoretically use fluid.getBlock(), but other mods like GregTech break it for some reason.

    public static void preInit() {
        registerFluidContainers();

        PneumaticRegistry.getInstance().registerFuel(OIL, 64000);
        PneumaticRegistry.getInstance().registerFuel(DIESEL, 700000);
        PneumaticRegistry.getInstance().registerFuel(KEROSENE, 1100000);
        PneumaticRegistry.getInstance().registerFuel(GASOLINE, 1500000);
        PneumaticRegistry.getInstance().registerFuel(LPG, 1800000);

        PneumaticRegistry.getInstance().registerPlasticFluid(PLASTIC, ConfigHandler.machineProperties.plasticMixerPlasticRatio);
    }

    public static void init() {
        // stuff that needs to be done AFTER items & blocks are registered
        PneumaticCraftAPIHandler.getInstance().registerRefineryInput(Fluids.OIL);

        // no magnet'ing PCB's out of etching acid pools
        PneumaticCraftAPIHandler.getInstance().getItemRegistry().registerMagnetSuppressor(
                e -> e instanceof EntityItem && ((EntityItem) e).getItem().getItem() == Itemss.EMPTY_PCB
                        && e.getEntityWorld().getBlockState(e.getPosition()).getBlock() == Fluids.ETCHING_ACID.getBlock()
        );
    }

    private static <T extends Block & IFluidBlock> Fluid createFluid(String name,
                                                                     Consumer<Fluid> fluidPropertyApplier, Function<Fluid, T> blockFactory) {
        try {
            Fluid fluid;
            Fluid preExistingFluid = FluidRegistry.getFluid(name);
            if (preExistingFluid != null) {
                fluid = preExistingFluid;
                Log.warning("Fluid '" + name + "' already registered by another mod: [" + FluidRegistry.getDefaultFluidName(fluid)
                        + "] - we'll use it, but it might not be available in block form!");
            } else {
                fluid = new FluidPneumaticCraft(name);
                FluidRegistry.registerFluid(fluid);
                fluidPropertyApplier.accept(fluid);
                MOD_FLUID_BLOCKS.add(blockFactory.apply(fluid));
            }
            FLUIDS.add(fluid);

            return fluid;
        } catch (Exception e) {
            PneumaticCraftRepressurized.logger.error(e.getMessage());
            return null;
        }
    }

    @Mod.EventBusSubscriber
    public static class RegistrationHandler {
        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            final IForgeRegistry<Block> registry = event.getRegistry();

            for (final IFluidBlock fluidBlock : MOD_FLUID_BLOCKS) {
                final Block block = (Block) fluidBlock;
                block.setRegistryName(RL("fluid." + fluidBlock.getFluid().getName()));
                block.setTranslationKey(fluidBlock.getFluid().getName());
                block.setCreativeTab(PneumaticCraftRepressurized.tabPneumaticCraft);
                registry.register(block);
                fluidToBlockMap.put(fluidBlock.getFluid().getName(), block);
                Log.info("registered block for fluid: " + fluidBlock.getFluid().getName());
            }
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            final IForgeRegistry<Item> registry = event.getRegistry();
            for (final IFluidBlock fluidBlock : MOD_FLUID_BLOCKS) {
                final Block block = (Block) fluidBlock;
                final ItemBlock itemBlock = new ItemBlock(block);
                itemBlock.setRegistryName(block.getRegistryName());
                registry.register(itemBlock);
            }
        }
    }

    public static boolean areFluidsEqual(Fluid fluid1, Fluid fluid2) {
        return fluid1 == null && fluid2 == null || fluid1 == null == (fluid2 == null) && fluid1.getName().equals(fluid2.getName());
    }

    public static Item getBucket(Fluid fluid) {
        return FluidUtil.getFilledBucket(new FluidStack(fluid, 1000)).getItem();
    }

    public static ItemStack getBucketStack(Fluid fluid) {
        return FluidUtil.getFilledBucket(new FluidStack(fluid, 1000));
    }

    public static Block getBlock(Fluid fluid) {
        return fluidToBlockMap.get(fluid.getName());
    }

    private static void registerFluidContainers() {
        for (final Fluid fluid : FLUIDS) {
            FluidRegistry.addBucketForFluid(fluid);
        }
    }
}
