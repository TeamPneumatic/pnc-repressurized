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

import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.registry.ModFluids;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.material.Fluid;

public class FluidSetup {
    /**
     * Fluid setup tasks to be done AFTER fluids (and items/blocks) are registered
     *
     * Note: fluid fuel values now all done via datapack
     */
    public static void init() {
        PneumaticCraftAPIHandler api = PneumaticCraftAPIHandler.getInstance();

        // register hot fluids as (very inefficient) fuels
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            try {
                int temperature = fluid.getFluidType().getTemperature();
                if (temperature >= ConfigHelper.common().general.minFluidFuelTemperature.get() && fluid.isSource(fluid.defaultFluidState())) {
                    // non-API usage... register an explicit fluid rather than a tag
                    FuelRegistry.getInstance().registerHotFluid(fluid, (temperature - 300) * 40, 0.25f);
                }
            } catch (RuntimeException e) {
                ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid);
                Log.error("Caught exception while checking the fluid type of %s: %s", fluidId, e.getMessage());
                Log.error("Looks like %s isn't setting a fluid type for this fluid, please report to the mod author", fluidId.getNamespace());
            }
        }

        // no magnet'ing PCB's out of etching acid pools
        api.getItemRegistry().registerMagnetSuppressor(
                e -> e instanceof ItemEntity ie && ie.getItem().getItem() == ModItems.EMPTY_PCB.get()
                        && e.getCommandSenderWorld().getFluidState(e.blockPosition()).getType() == ModFluids.ETCHING_ACID.get()
        );

        // note: default "forge:experience" now added in EventHandlerPneumaticCraft#onTagsUpdated
    }
}
