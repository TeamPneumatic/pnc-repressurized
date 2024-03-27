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

package me.desht.pneumaticcraft.api.drone;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Fired (on the MinecraftForge.EVENT_BUS) when
 * {@link IDroneRegistry#retrieveItemsAmazonStyle(GlobalPos, ItemStack...)} or
 * {@link IDroneRegistry#retrieveFluidAmazonStyle(GlobalPos, FluidStack)}
 * has successfully retrieved the items requested.  The drone passed to this event is the same as the one returned by
 * the retrieval method.
 */
public class AmadronRetrievalEvent extends Event {
    public final IDrone drone;

    public AmadronRetrievalEvent(IDrone drone) {
        this.drone = drone;
    }
}
