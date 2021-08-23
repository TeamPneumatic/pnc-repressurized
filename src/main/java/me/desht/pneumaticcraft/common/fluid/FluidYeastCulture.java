package me.desht.pneumaticcraft.common.fluid;

import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class FluidYeastCulture {
    private static final FluidAttributes.Builder ATTRS = FluidAttributes.builder(
            RL("block/fluid/generic_fuel_still"), RL("block/fluid/generic_fuel_flow")
    ).density(800).viscosity(10000).temperature(300).color(0xFFE2D2B8);

    private static final ForgeFlowingFluid.Properties PROPS =
            new ForgeFlowingFluid.Properties(ModFluids.YEAST_CULTURE, ModFluids.YEAST_CULTURE_FLOWING, ATTRS)
                    .block(ModBlocks.YEAST_CULTURE).bucket(ModItems.YEAST_CULTURE_BUCKET);

    public static class Source extends ForgeFlowingFluid.Source {
        public Source() {
            super(PROPS);
        }

        @Override
        public int getTickDelay(IWorldReader world) {
            return 30;
        }

        @Override
        public void tick(World worldIn, BlockPos pos, FluidState state) {
            if (PNCConfig.Common.Recipes.inWorldYeastCrafting) {
                List<ItemEntity> entities = worldIn.getEntitiesOfClass(ItemEntity.class, new AxisAlignedBB(pos), e -> e.getItem().getItem() == Items.SUGAR);
                if (!entities.isEmpty()) {
                    for (Direction d : DirectionUtil.VALUES) {
                        BlockPos pos1 = pos.relative(d);
                        FluidState fluidState = worldIn.getFluidState(pos1);
                        if (fluidState.isSource() && fluidState.getType() == Fluids.WATER && worldIn.getBlockState(pos1).getBlock() == Blocks.WATER) {
                            worldIn.setBlock(pos1, ModFluids.YEAST_CULTURE.get().defaultFluidState().createLegacyBlock(), Constants.BlockFlags.DEFAULT);
                            entities.get(0).getItem().shrink(1);
                            if (entities.get(0).getItem().isEmpty()) {
                                entities.get(0).remove();
                            }
                            break;
                        }
                    }
                }
            }
            super.tick(worldIn, pos, state);
        }
    }

    public static class Flowing extends ForgeFlowingFluid.Flowing {
        public Flowing() {
            super(PROPS);
        }
    }
}
