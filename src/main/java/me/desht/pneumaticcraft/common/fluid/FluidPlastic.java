package me.desht.pneumaticcraft.common.fluid;

import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ICustomTooltipName;
import me.desht.pneumaticcraft.common.item.ItemBucketPneumaticCraft;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public abstract class FluidPlastic {
    private static final FluidAttributes.Builder ATTRS = FluidAttributes.builder(
            RL("block/fluid/plastic_still"), RL("block/fluid/plastic_flow")
    ).temperature(PneumaticValues.PLASTIC_MIXER_MELTING_TEMP);

    private static final ForgeFlowingFluid.Properties PROPS =
            new ForgeFlowingFluid.Properties(ModFluids.PLASTIC, ModFluids.PLASTIC_FLOWING, ATTRS)
                    .block(ModBlocks.PLASTIC).bucket(ModItems.PLASTIC_BUCKET);

    public static class Source extends ForgeFlowingFluid.Source {
        public Source() {
            super(PROPS);
        }

        @Override
        public int getTickDelay(IWorldReader world) {
            return 10;
        }

        @Override
        public void tick(World worldIn, BlockPos pos, FluidState state) {
            if (PNCConfig.Common.Recipes.inWorldPlasticSolidification) {
                ItemEntity item = new ItemEntity(worldIn, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, new ItemStack(ModItems.PLASTIC.get()));
                worldIn.addFreshEntity(item);
                worldIn.setBlock(pos, Blocks.AIR.defaultBlockState(), Constants.BlockFlags.DEFAULT);
            }
            super.tick(worldIn, pos, state);
        }
    }

    public static class Flowing extends ForgeFlowingFluid.Flowing {
        public Flowing() {
            super(PROPS);
        }
    }

    public static class Bucket extends ItemBucketPneumaticCraft implements ICustomTooltipName {
        public Bucket() {
            super(ModFluids.PLASTIC);
        }

        @Override
        public String getCustomTooltipTranslationKey() {
            return PNCConfig.Common.Recipes.inWorldPlasticSolidification ? getDescriptionId() : getDescriptionId() + ".not_in_world";
        }
    }
}
