package me.desht.pneumaticcraft.common.fluid;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public abstract class FluidEtchingAcid extends ForgeFlowingFluid {
    private static final FluidAttributes.Builder ATTRS = FluidAttributes.builder(
//            RL("block/fluid/etchacid_still"), RL("block/fluid/etchacid_flow")
            new ResourceLocation("minecraft:block/water_still"),
            new ResourceLocation("minecraft:block/water_flow")
    ).color(MaterialColor.EMERALD.colorValue | 0xFF000000);

    private static final ForgeFlowingFluid.Properties PROPS =
            new ForgeFlowingFluid.Properties(ModFluids.ETCHING_ACID, ModFluids.ETCHING_ACID_FLOWING, ATTRS)
                    .block(ModBlocks.ETCHING_ACID)
                    .bucket(ModItems.ETCHING_ACID_BUCKET
            );

    FluidEtchingAcid() {
        super(PROPS);
    }

    public static class Source extends FluidEtchingAcid {
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
