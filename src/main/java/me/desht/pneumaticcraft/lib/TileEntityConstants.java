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

package me.desht.pneumaticcraft.lib;

public class TileEntityConstants {
    public static final float CANNON_TURN_HIGH_SPEED = 3.0F;
    public static final float CANNON_TURN_LOW_SPEED = 0.5F;
    public static final float CANNON_SLOW_ANGLE = 20F;

    public static final float ELEVATOR_SPEED_SLOW = 0.02F;
    public static final float ELEVATOR_SPEED_FAST = 0.05F;
    public static final float ELEVATOR_SLOW_EXTENSION = 0.5F;

    public static final float PNEUMATIC_DOOR_SPEED_SLOW = 0.01F;
    public static final float PNEUMATIC_DOOR_SPEED_FAST = 0.04F;
    public static final float PNEUMATIC_DOOR_EXTENSION = 0.15F;
    public static final int RANGE_PNEUMATIC_DOOR_BASE = 2;

    public static final float ASSEMBLY_IO_UNIT_ARM_SPEED = 1.0F;
    public static final float ASSEMBLY_IO_UNIT_CLAW_SPEED = 0.05F;

    public static final float ASSEMBLY_DRILL_ACCELERATION = 1F;
    public static final float ASSEMBLY_DRILL_MAX_SPEED = 100F;

    public static final int PCB_ETCH_TIME = 300; //seconds

    public static final float NETWORK_NORMAL_BRIDGE_SPEED = 0.02F;//*100 % / tick
    public static final float NETWORK_AI_BRIDGE_SPEED = 0.03F;//*100 % / tick
    public static final float NETWORK_NODE_RATING_MULTIPLIER = 1.2F;
    public static final int SECURITY_STATION_MAX_RANGE = 16;
    public static final int SECURITY_STATION_REBOOT_TIME = 1200;//ticks

    public static final double PACKET_UPDATE_DISTANCE = 64D;//maximum client distance before clients aren't being sent anymore from the server.

    public static final int HEAT_SINK_THERMAL_RESISTANCE = 14;
}
