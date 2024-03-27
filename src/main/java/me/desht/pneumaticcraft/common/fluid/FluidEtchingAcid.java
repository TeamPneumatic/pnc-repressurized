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

import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModFluids;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class FluidEtchingAcid {
    public static final PNCFluidRenderProps RENDER_PROPS = new PNCFluidRenderProps
            ("minecraft:block/water_still", "minecraft:block/water_flow", MapColor.EMERALD.col | 0xFF000000);

    private static BaseFlowingFluid.Properties props() {
        return new BaseFlowingFluid.Properties(
                ModFluids.ETCHING_ACID_FLUID_TYPE, ModFluids.ETCHING_ACID, ModFluids.ETCHING_ACID_FLOWING
        ).block(ModBlocks.ETCHING_ACID).bucket(ModItems.ETCHING_ACID_BUCKET);
    }

    public static class Source extends BaseFlowingFluid.Source {
        public Source() {
            super(props());
        }
    }

    public static class Flowing extends BaseFlowingFluid.Flowing {
        public Flowing() {
            super(props());
        }
    }
}
