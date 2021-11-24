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

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public abstract class FluidEtchingAcid {
    private static final FluidAttributes.Builder ATTRS = FluidAttributes.builder(
            new ResourceLocation("minecraft:block/water_still"),
            new ResourceLocation("minecraft:block/water_flow")
    ).color(MaterialColor.EMERALD.col | 0xFF000000);

    private static final ForgeFlowingFluid.Properties PROPS =
            new ForgeFlowingFluid.Properties(ModFluids.ETCHING_ACID, ModFluids.ETCHING_ACID_FLOWING, ATTRS)
                    .block(ModBlocks.ETCHING_ACID)
                    .bucket(ModItems.ETCHING_ACID_BUCKET);

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
