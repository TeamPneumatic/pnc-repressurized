package me.desht.pneumaticcraft.common.fluid;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.state.StateContainer;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public abstract class FluidLPG extends ForgeFlowingFluid {
    private static final FluidAttributes.Builder ATTRS = FluidAttributes.builder(
            RL("block/fluid/lpg_still"), RL("block/fluid/lpg_flow")
    ).viscosity(250);

    private static final ForgeFlowingFluid.Properties PROPS =
            new ForgeFlowingFluid.Properties(ModFluids.LPG, ModFluids.LPG_FLOWING, ATTRS)
                    .block(ModBlocks.LPG).bucket(ModItems.LPG_BUCKET
            );

    FluidLPG() {
        super(PROPS);
    }

    @Override
    public int getTickRate(IWorldReader world) {
        return 2;
    }

    public static class Source extends FluidLPG {
        @Override
        public boolean isSource(IFluidState state) {
            return true;
        }

        @Override
        public int getLevel(IFluidState state) {
            return 8;
        }
    }

    public static class Flowing extends FluidLPG {
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
