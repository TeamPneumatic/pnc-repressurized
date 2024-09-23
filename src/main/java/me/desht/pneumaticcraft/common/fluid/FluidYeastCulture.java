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
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModFluids;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

import java.util.List;

public class FluidYeastCulture {
    public static final PNCFluidRenderProps RENDER_PROPS = PNCFluidRenderProps.genericFuel(0xFFE2D2B8);

    private static BaseFlowingFluid.Properties props() {
        return new BaseFlowingFluid.Properties(
                ModFluids.YEAST_CULTURE_FLUID_TYPE, ModFluids.YEAST_CULTURE, ModFluids.YEAST_CULTURE_FLOWING
        ).block(ModBlocks.YEAST_CULTURE).bucket(ModItems.YEAST_CULTURE_BUCKET).tickRate(30);
    }

    public static class Source extends BaseFlowingFluid.Source {
        public Source() {
            super(props());
        }

        @Override
        public void tick(Level worldIn, BlockPos pos, FluidState state) {
            if (ConfigHelper.common().recipes.inWorldYeastCrafting.get()) {
                List<ItemEntity> entities = worldIn.getEntitiesOfClass(ItemEntity.class, new AABB(pos), e -> e.getItem().getItem() == Items.SUGAR);
                if (!entities.isEmpty()) {
                    for (Direction d : DirectionUtil.VALUES) {
                        BlockPos pos1 = pos.relative(d);
                        FluidState fluidState = worldIn.getFluidState(pos1);
                        if (fluidState.isSource() && fluidState.is(FluidTags.WATER) && worldIn.getBlockState(pos1).getBlock() == Blocks.WATER) {
                            worldIn.setBlock(pos1, ModFluids.YEAST_CULTURE.get().defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL);
                            ItemEntity sugarItemEntity = entities.getFirst();
                            sugarItemEntity.getItem().shrink(1);
                            if (sugarItemEntity.getItem().isEmpty()) {
                                sugarItemEntity.discard();
                            }
                            break;
                        }
                    }
                }
            }
            super.tick(worldIn, pos, state);
        }
    }

    public static class Flowing extends BaseFlowingFluid.Flowing {
        public Flowing() {
            super(props());
        }
    }
}
