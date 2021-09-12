package me.desht.pneumaticcraft.common.fluid;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public abstract class FluidMemoryEssence {
    private static final FluidAttributes.Builder ATTRS = FluidAttributes.builder(
            RL("block/fluid/memory_essence_still"), RL("block/fluid/memory_essence_flow")
    ).color(0xFFD0FF00);

    private static final ForgeFlowingFluid.Properties PROPS =
            new ForgeFlowingFluid.Properties(ModFluids.MEMORY_ESSENCE, ModFluids.MEMORY_ESSENCE_FLOWING, ATTRS)
                    .block(ModBlocks.MEMORY_ESSENCE).bucket(ModItems.MEMORY_ESSENCE_BUCKET);


    public static class Source extends ForgeFlowingFluid.Source {
        public Source() {
            super(PROPS);
        }
    }

    public static class Flowing extends ForgeFlowingFluid.Flowing {
        public Flowing() {
            super(PROPS);
        }
    }
}
