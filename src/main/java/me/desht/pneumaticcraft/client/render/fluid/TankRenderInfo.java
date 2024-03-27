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

package me.desht.pneumaticcraft.client.render.fluid;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.BitSet;

public class TankRenderInfo {
    private final IFluidTank tank;
    private final AABB bounds;
    private final BitSet faces = new BitSet(6);

    public TankRenderInfo(FluidStack stack, int capacity, AABB bounds, Direction... renderFaces) {
        FluidTank tank = new FluidTank(capacity);
        tank.setFluid(stack);
        this.tank = tank;
        this.bounds = bounds;
        if (renderFaces.length == 0) {
            faces.set(0, 6, true);
        } else {
            for (Direction face : renderFaces) {
                faces.set(face.get3DDataValue(), true);
            }
        }
    }

    public TankRenderInfo(IFluidTank tank, AABB bounds, Direction... renderFaces) {
        this(tank.getFluid(), tank.getCapacity(), bounds, renderFaces);
    }

    public TankRenderInfo without(Direction face) {
        faces.clear(face.get3DDataValue());
        return this;
    }

    public boolean shouldRender(Direction face) {
        return faces.get(face.get3DDataValue());
    }

    public IFluidTank getTank() {
        return tank;
    }

    public AABB getBounds() {
        return bounds;
    }

    public BitSet getFaces() {
        return faces;
    }
}
