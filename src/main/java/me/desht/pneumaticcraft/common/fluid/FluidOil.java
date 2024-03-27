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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class FluidOil {
    public static final PNCFluidRenderProps RENDER_PROPS = new PNCFluidRenderProps("oil_still", "oil_flow");

    private static BaseFlowingFluid.Properties props() {
        return new BaseFlowingFluid.Properties(
                ModFluids.OIL_FLUID_TYPE, ModFluids.OIL, ModFluids.OIL_FLOWING
        ).block(ModBlocks.OIL).bucket(ModItems.OIL_BUCKET).tickRate(20);
    }

    public static class Source extends BaseFlowingFluid.Source {
        public Source() {
            super(props());
        }

        @Override
        public boolean move(FluidState state, LivingEntity entity, Vec3 movementVector, double gravity) {
            // based on lava movement
            double y = entity.getY();
            boolean falling = entity.getDeltaMovement().y <= 0.0D;
            entity.moveRelative(0.02F, movementVector);
            entity.move(MoverType.SELF, entity.getDeltaMovement());
            if (entity.getFluidTypeHeight(getFluidType()) <= entity.getFluidJumpThreshold()) {
                entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.5D, 0.8D, 0.5D));
                Vec3 fallingMovement = entity.getFluidFallingAdjustedMovement(gravity, falling, entity.getDeltaMovement());
                entity.setDeltaMovement(fallingMovement);
            } else {
                entity.setDeltaMovement(entity.getDeltaMovement().scale(0.5D));
            }

            if (!entity.isNoGravity()) {
                entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, -gravity / 4.0D, 0.0D));
            }

            Vec3 delta = entity.getDeltaMovement();
            if (entity.horizontalCollision && entity.isFree(delta.x, delta.y + (double)0.6F - entity.getY() + y, delta.z)) {
                entity.setDeltaMovement(delta.x, 0.3D, delta.z);
            }
            return true;
        }
    }

    public static class Flowing extends BaseFlowingFluid.Flowing {
        public Flowing() {
            super(props());
        }
    }
}
