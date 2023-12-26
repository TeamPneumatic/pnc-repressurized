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

package me.desht.pneumaticcraft.api.lib;

import net.minecraft.resources.ResourceLocation;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class Names {
    public static final String MOD_ID = "pneumaticcraft";
    public static final String MOD_NAME = "PneumaticCraft: Repressurized";

    public static final ResourceLocation MODULE_SAFETY_VALVE = RL("safety_tube_module");
    public static final ResourceLocation MODULE_REGULATOR = RL("regulator_tube_module");
    public static final ResourceLocation MODULE_GAUGE = RL("pressure_gauge_module");
    public static final ResourceLocation MODULE_FLOW_DETECTOR = RL("flow_detector_module");
    public static final ResourceLocation MODULE_AIR_GRATE = RL("air_grate_module");
    public static final ResourceLocation MODULE_CHARGING = RL("charging_module");
    public static final ResourceLocation MODULE_LOGISTICS = RL("logistics_module");
    public static final ResourceLocation MODULE_REDSTONE = RL("redstone_module");
    public static final ResourceLocation MODULE_VACUUM = RL("vacuum_module");
    public static final ResourceLocation MODULE_THERMOSTAT = RL("thermostat_module");

    public static final String PNEUMATIC_KEYBINDING_CATEGORY_MAIN = "key.pneumaticcraft.category.main";
    public static final String PNEUMATIC_KEYBINDING_CATEGORY_UPGRADE_TOGGLES = "key.pneumaticcraft.category.upgrade_toggles";
    public static final String PNEUMATIC_KEYBINDING_CATEGORY_BLOCK_TRACKER = "key.pneumaticcraft.category.block_tracker";

    // Agreed by convention among several mods to denote item entities which should not be magnet'ed
    public static final String PREVENT_REMOTE_MOVEMENT = "PreventRemoteMovement";
}
