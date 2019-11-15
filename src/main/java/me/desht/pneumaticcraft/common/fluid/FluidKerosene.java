package me.desht.pneumaticcraft.common.fluid;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.state.StateContainer;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public abstract class FluidKerosene extends ForgeFlowingFluid {
    private static final FluidAttributes.Builder ATTRS = FluidAttributes.builder(
            RL("fluid/kerosene_still"), RL("fluid/kerosene_flow")
    ).color(MaterialColor.LIGHT_BLUE.colorValue).viscosity(750);

    private static final ForgeFlowingFluid.Properties PROPS =
            new ForgeFlowingFluid.Properties(
                    () -> ModFluids.KEROSENE_SOURCE, () -> ModFluids.KEROSENE_FLOWING, ATTRS)
                    .block(() -> ModBlocks.KEROSENE_BLOCK).bucket(() -> ModItems.KEROSENE_BUCKET
            );

    FluidKerosene(String name) {
        super(PROPS);

        setRegistryName(RL(name));
    }

    public static class Source extends FluidKerosene {
        public Source() {
            super("kerosene_source");
        }

        @Override
        public boolean isSource(IFluidState state) {
            return true;
        }

        @Override
        public int getLevel(IFluidState state) {
            return 8;
        }
    }

    public static class Flowing extends FluidKerosene {
        public Flowing() {
            super("kerosene_flowing");
        }

        @Override
        protected void fillStateContainer(StateContainer.Builder<Fluid, IFluidState> builder) {
            super.fillStateContainer(builder);
            builder.add(LEVEL_1_8);
        }

        @Override
        public boolean isSource(IFluidState state) {
            return false;
        }

        @Override
        public int getLevel(IFluidState state) {
            return state.get(LEVEL_1_8);
        }
    }
}
