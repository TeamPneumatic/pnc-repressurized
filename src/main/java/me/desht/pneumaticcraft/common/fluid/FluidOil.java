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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public abstract class FluidOil {
    public static final PNCFluidRenderProps RENDER_PROPS = new PNCFluidRenderProps("oil_still", "oil_flow");

    private static ForgeFlowingFluid.Properties props() {
        return new ForgeFlowingFluid.Properties(
                ModFluids.OIL_FLUID_TYPE, ModFluids.OIL, ModFluids.OIL_FLOWING
        ).block(ModBlocks.OIL).bucket(ModItems.OIL_BUCKET).tickRate(20);
    }

    public static class Source extends ForgeFlowingFluid.Source {
        public Source() {
            super(props());
        }

        @Override
        public boolean move(FluidState state, LivingEntity entity, Vec3 movementVector, double gravity) {
            return super.move(state, entity, movementVector, gravity);
        }
    }

    public static class Flowing extends ForgeFlowingFluid.Flowing {
        public Flowing() {
            super(props());
        }
    }
}
