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
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class FluidLPG {
    public static final PNCFluidRenderProps RENDER_PROPS = PNCFluidRenderProps.genericFuel(0xD0ECD71C);

    private static BaseFlowingFluid.Properties props() {
        return new BaseFlowingFluid.Properties(
                ModFluids.LPG_FLUID_TYPE, ModFluids.LPG, ModFluids.LPG_FLOWING
        ).block(ModBlocks.LPG).bucket(ModItems.LPG_BUCKET).tickRate(2);
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
