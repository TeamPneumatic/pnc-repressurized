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

package me.desht.pneumaticcraft.api.tileentity;

import me.desht.pneumaticcraft.api.pressure.PressureTier;
import net.minecraft.nbt.CompoundTag;

/**
 * Use this interface to get instances of air handlers for your tile entities.  You can then expose those air handler
 * instances via the {@link IAirHandlerMachine} capability interface; {@link me.desht.pneumaticcraft.api.PNCCapabilities#AIR_HANDLER_MACHINE} can be used for this.
 * <p>
 * Get an instance of this factory with
 * {@link me.desht.pneumaticcraft.api.PneumaticRegistry.IPneumaticCraftInterface#getAirHandlerMachineFactory()}.
 */
public interface IAirHandlerMachineFactory {
    /**
     * Create a standard tier one air handler.
     *
     * @param volume the air handler volume, in mL.
     * @return a new tier one air handler
     */
    IAirHandlerMachine createTierOneAirHandler(int volume);

    /**
     * Create a standard tier two air handler.
     *
     * @param volume the air handler volume, in mL.
     * @return a new tier two air handler
     */
    IAirHandlerMachine createTierTwoAirHandler(int volume);

    /**
     * Returns a new instance of an IAirHandler. This handler handles everything pressurized air related: air dispersion,
     * blowing up when the pressure gets too high, providing a method for releasing air into the atmosphere...
     * <strong>provided that you take the following steps:</strong>
     * <ul>
     *     <li>Storing this object as a field in your block entity</li>
     *     <li>Providing capability-based access to it via {@link me.desht.pneumaticcraft.api.PNCCapabilities#AIR_HANDLER_MACHINE}</li>
     *     <li>Loading and saving the air handler in {@link net.minecraft.world.level.block.entity.BlockEntity#load(CompoundTag)}
     *     and {@link net.minecraft.world.level.block.entity.BlockEntity#saveAdditional(CompoundTag)} </li>
     *     <li>Ticking the air handler in your block entity's tick implementation (your block entity <strong>must</strong> be ticked)</li>
     *</ul>
     *
     * @param tier   the pressure tier
     * @param volume volume of the machine's internal storage; the pressure (in bar) is the actual amount of air in the machine divided by its volume
     * @return the air handler object
     */
    IAirHandlerMachine createAirHandler(PressureTier tier, int volume);
}
