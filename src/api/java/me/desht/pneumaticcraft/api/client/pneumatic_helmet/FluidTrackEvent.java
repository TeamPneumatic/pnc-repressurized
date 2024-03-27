/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * Fired when a helmet Block Tracker is about to track a fluid tank. Can be canceled to prevent tracking.
 * Posted on MinecraftForge.EVENT_BUS
 *
 * The tile inventory is known to support CapabilityFluidHandler.FLUID_CAPABILITY on at least one face
 * when the event is received.
 *
 * @author MineMaarten
 */
public class FluidTrackEvent extends Event implements ICancellableEvent {
    private final BlockEntity te;

    public FluidTrackEvent(BlockEntity te) {
        this.te = te;
    }

    public IFluidHandler getFluidHandler() {
        return getFluidHandler(null);
    }

    public IFluidHandler getFluidHandler(Direction face) {
        return te.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, te.getBlockPos(), te.getBlockState(), te, face);
    }
}
