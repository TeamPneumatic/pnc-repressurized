/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.fluid;

import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.item.ICustomTooltipName;
import me.desht.pneumaticcraft.common.item.PneumaticCraftBucketItem;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModFluids;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class FluidPlastic {
    public static final PNCFluidRenderProps RENDER_PROPS = new PNCFluidRenderProps("plastic_still", "plastic_flow");

    private static BaseFlowingFluid.Properties props() {
        return new BaseFlowingFluid.Properties(
                ModFluids.PLASTIC_FLUID_TYPE, ModFluids.PLASTIC, ModFluids.PLASTIC_FLOWING
        ).block(ModBlocks.PLASTIC).bucket(ModItems.PLASTIC_BUCKET).tickRate(10);
    }

    public static class Source extends BaseFlowingFluid.Source {
        public Source() {
            super(props());
        }

        @Override
        public void tick(Level worldIn, BlockPos pos, FluidState state) {
            if (ConfigHelper.common().recipes.inWorldPlasticSolidification.get()) {
                ItemEntity item = new ItemEntity(worldIn, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, new ItemStack(ModItems.PLASTIC.get()));
                worldIn.addFreshEntity(item);
                worldIn.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            super.tick(worldIn, pos, state);
        }
    }

    public static class Flowing extends BaseFlowingFluid.Flowing {
        public Flowing() {
            super(props());
        }
    }

    public static class Bucket extends PneumaticCraftBucketItem implements ICustomTooltipName {
        public Bucket() {
            super(ModFluids.PLASTIC.get());
        }

        @Override
        public String getCustomTooltipTranslationKey() {
            return ConfigHelper.common().recipes.inWorldPlasticSolidification.get() ? getDescriptionId() : getDescriptionId() + ".not_in_world";
        }
    }
}
