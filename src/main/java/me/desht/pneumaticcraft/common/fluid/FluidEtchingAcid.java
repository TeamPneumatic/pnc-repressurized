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

public abstract class FluidEtchingAcid extends ForgeFlowingFluid {
    private static final FluidAttributes.Builder ATTRS = FluidAttributes.builder(
            RL("fluid/etchacid_still"), RL("fluid/etchacid_flow")
    ).color(MaterialColor.BROWN.colorValue);

    private static final ForgeFlowingFluid.Properties PROPS =
            new ForgeFlowingFluid.Properties(
                    () -> ModFluids.ETCHING_ACID_SOURCE, () -> ModFluids.ETCHING_ACID_FLOWING, ATTRS)
                    .block(() -> ModBlocks.ETCHING_ACID_BLOCK).bucket(() -> ModItems.ETCHING_ACID_BUCKET
            );

    FluidEtchingAcid(String name) {
        super(PROPS);

        setRegistryName(name);
    }

    public static class Source extends FluidEtchingAcid {
        public Source() {
            super("etching_acid_source");
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

    public static class Flowing extends FluidEtchingAcid {
        public Flowing() {
            super("etching_acid_flowing");
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
