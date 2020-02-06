package me.desht.pneumaticcraft.common.fluid;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public abstract class FluidPlastic extends ForgeFlowingFluid {
    private static final FluidAttributes.Builder ATTRS = FluidAttributes.builder(
            RL("block/fluid/plastic_still"), RL("block/fluid/plastic_flow")
    ).temperature(PneumaticValues.PLASTIC_MIXER_MELTING_TEMP);

    private static final ForgeFlowingFluid.Properties PROPS =
            new ForgeFlowingFluid.Properties(ModFluids.PLASTIC, ModFluids.PLASTIC_FLOWING, ATTRS)
                    .block(ModBlocks.PLASTIC).bucket(ModItems.PLASTIC_BUCKET);

    FluidPlastic() {
        super(PROPS);
    }

    @Override
    public int getTickRate(IWorldReader world) {
        return 10;
    }

    public static class Source extends FluidPlastic {
        @Override
        public boolean isSource(IFluidState state) {
            return true;
        }

        @Override
        public int getLevel(IFluidState state) {
            return 8;
        }

        @Override
        public void tick(World worldIn, BlockPos pos, IFluidState state) {
            ItemEntity item = new ItemEntity(worldIn, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, new ItemStack(ModItems.PLASTIC.get()));
            worldIn.addEntity(item);
            worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), Constants.BlockFlags.DEFAULT);
            super.tick(worldIn, pos, state);
        }
    }

    public static class Flowing extends FluidPlastic {
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
