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

package me.desht.pneumaticcraft.common.advancements;

import net.minecraft.advancements.CriteriaTriggers;

import java.util.Arrays;

public class AdvancementTriggers {
    public static final CustomTrigger EXPLODE_IRON = new CustomTrigger("root");
    public static final CustomTrigger OIL_BUCKET = new CustomTrigger("oil_bucket");
    public static final CustomTrigger NINEBYNINE = new CustomTrigger("9x9");
    public static final CustomTrigger PRESSURE_CHAMBER = new CustomTrigger("pressure_chamber");
    public static final CustomTrigger PROGRAM_DRONE = new CustomTrigger("program_drone");
    public static final CustomTrigger PNEUMATIC_ARMOR = new CustomTrigger("pneumatic_armor");
    public static final CustomTrigger ENTITY_HACK = new CustomTrigger("entity_hack");
    public static final CustomTrigger BLOCK_HACK = new CustomTrigger("block_hack");
    public static final CustomTrigger FLIGHT = new CustomTrigger("flight");
    public static final CustomTrigger FLY_INTO_WALL = new CustomTrigger("fly_into_wall");
    public static final CustomTrigger LOGISTICS_DRONE_DEPLOYED = new CustomTrigger("logistics_drone_deployed");
    public static final CustomTrigger CHARGED_WRENCH = new CustomTrigger("charged_wrench");
    public static final CustomTrigger MACHINE_VANDAL = new CustomTrigger("machine_vandal");

    private static final CustomTrigger[] ALL_TRIGGERS = new CustomTrigger[] {
            EXPLODE_IRON,
            OIL_BUCKET,
            NINEBYNINE,
            PRESSURE_CHAMBER,
            PROGRAM_DRONE,
            PNEUMATIC_ARMOR,
            ENTITY_HACK,
            BLOCK_HACK,
            FLIGHT,
            FLY_INTO_WALL,
            LOGISTICS_DRONE_DEPLOYED,
            CHARGED_WRENCH,
            MACHINE_VANDAL
    };

    public static void registerTriggers() {
        Arrays.stream(ALL_TRIGGERS).forEach(CriteriaTriggers::register);
    }
}
