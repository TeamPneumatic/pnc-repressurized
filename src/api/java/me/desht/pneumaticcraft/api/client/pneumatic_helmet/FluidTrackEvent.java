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

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * Fired when a helmet Block Tracker is about to track a fluid tank. Can be canceled to prevent tracking.
 * Posted on MinecraftForge.EVENT_BUS
 *
 * The tile inventory is known to support CapabilityFluidHandler.FLUID_CAPABILITY on at least one face
 * when the event is received.
 *
 * @author MineMaarten
 */
@Cancelable
public class FluidTrackEvent extends Event {
    private final TileEntity te;

    public FluidTrackEvent(TileEntity te) {
        this.te = te;
    }

    public IFluidHandler getFluidHandler() {
        return getFluidHandler(null);
    }

    public IFluidHandler getFluidHandler(Direction face) {
        return te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face).map(iFluidHandler -> iFluidHandler).orElse(null);
    }
}
