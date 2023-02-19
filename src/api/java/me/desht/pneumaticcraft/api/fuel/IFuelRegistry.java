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

package me.desht.pneumaticcraft.api.fuel;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import java.util.Collection;

public interface IFuelRegistry {
    /**
     * Get the fuel value of the given fluid; this is defined as the amount of compressed air generated in a Liquid
     * Compressor (without Speed Upgrades) by burning 1000mL of the fluid.
     *
     * @param level the level, for recipe lookup purposes
     * @param fluid the fluid
     * @return the fuel value (0 for fluids which have not been registered as fuels)
     */
    int getFuelValue(Level level, Fluid fluid);

    /**
     * Get the burn rate of the given fluid. Higher burn rates will generate compressed air faster in a Liquid
     * Compressor (and be used up faster), without affecting the total amount of air produced.
     *
     * @param level the level, for recipe lookup purposes
     * @param fluid the fluid
     * @return the burn rate (1.0f is the standard, default, rate)
     */
    float getBurnRateMultiplier(Level level, Fluid fluid);

    /**
     * Get all the known fuels which have been registered with the fuel registry. In the case of conflicts, precedence
     * is as follows: 1) fluids from datapacks, 2) fluid tags from datapacks, 3) fluids from API registration
     * (including built-in), 4) fluid tags from API registration
     *
     * <p>Note: this is a relatively expensive operation and should be used with care.  Use {@link #getFuelValue(Level, Fluid)}
     * if you just need the value of a known fluid.
     *
     * @param level the level, for recipe lookup purposes
     * @return a collection of fuel fluids
     */
    Collection<Fluid> registeredFuels(Level level);
}
