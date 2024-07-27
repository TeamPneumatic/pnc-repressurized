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

public class PneumaticValues {

    // danger pressures (bar)
    public static final float DANGER_PRESSURE_TIER_ONE = 5;
    public static final float DANGER_PRESSURE_PRESSURE_TUBE = DANGER_PRESSURE_TIER_ONE;
    public static final float DANGER_PRESSURE_AIR_COMPRESSOR = DANGER_PRESSURE_TIER_ONE;
    public static final float DANGER_PRESSURE_MANUAL_COMPRESSOR = DANGER_PRESSURE_TIER_ONE;
    public static final float DANGER_PRESSURE_AIR_CANNON = DANGER_PRESSURE_TIER_ONE;
    public static final float DANGER_PRESSURE_PRESSURE_CHAMBER = DANGER_PRESSURE_TIER_ONE;
    public static final float DANGER_PRESSURE_ELEVATOR = DANGER_PRESSURE_TIER_ONE;
    public static final float DANGER_PRESSURE_VACUUM_PUMP = DANGER_PRESSURE_TIER_ONE;
    public static final float DANGER_PRESSURE_PNEUMATIC_DOOR = DANGER_PRESSURE_TIER_ONE;
    public static final float DANGER_PRESSURE_ASSEMBLY_CONTROLLER = DANGER_PRESSURE_TIER_ONE;
    public static final float DANGER_PRESSURE_UV_LIGHTBOX = DANGER_PRESSURE_TIER_ONE;
    public static final float DANGER_PRESSURE_UNIVERSAL_SENSOR = DANGER_PRESSURE_TIER_ONE;
    public static final float DANGER_PRESSURE_THERMAL_COMPRESSOR = DANGER_PRESSURE_TIER_ONE;

    public static final float DANGER_PRESSURE_TIER_TWO = 20;
    public static final float DANGER_PRESSURE_ADVANCED_PRESSURE_TUBE = DANGER_PRESSURE_TIER_TWO;
    public static final float DANGER_PRESSURE_CHARGING_STATION = DANGER_PRESSURE_TIER_TWO;
//    public static final float DANGER_PRESSURE_PNEUMATIC_GENERATOR = DANGER_PRESSURE_TIER_TWO;
//    public static final float DANGER_PRESSURE_ELECTRIC_COMPRESSOR = DANGER_PRESSURE_TIER_TWO;
//    public static final float DANGER_PRESSURE_PNEUMATIC_ENGINE = DANGER_PRESSURE_TIER_TWO;
//    public static final float DANGER_PRESSURE_KINETIC_COMPRESSOR = DANGER_PRESSURE_TIER_TWO;
    public static final float DANGER_PRESSURE_PNEUMATIC_DYNAMO = DANGER_PRESSURE_TIER_TWO;
    public static final float DANGER_PRESSURE_FLUX_COMPRESSOR = DANGER_PRESSURE_TIER_TWO;
    public static final float DANGER_PRESSURE_SOLAR_COMPRESSOR = DANGER_PRESSURE_TIER_TWO;
    public static final float DANGER_PRESSURE_AERIAL_INTERFACE = DANGER_PRESSURE_TIER_TWO;
    public static final float DANGER_PRESSURE_ELECTROSTATIC_COMPRESSOR = DANGER_PRESSURE_TIER_TWO;
//    public static final float DANGER_PRESSURE_PNEUMATIC_PUMP = DANGER_PRESSURE_TIER_TWO;

    // critical pressures (bar)
    public static final float MAX_PRESSURE_TIER_ONE = 7F;
    public static final float MAX_PRESSURE_PRESSURE_TUBE = MAX_PRESSURE_TIER_ONE;
    public static final float MAX_PRESSURE_AIR_COMPRESSOR = MAX_PRESSURE_TIER_ONE;
    public static final float MAX_PRESSURE_MANUAL_COMPRESSOR = MAX_PRESSURE_TIER_ONE;
    public static final float MAX_PRESSURE_AIR_CANNON = MAX_PRESSURE_TIER_ONE;
    public static final float MAX_PRESSURE_PRESSURE_CHAMBER = MAX_PRESSURE_TIER_ONE;
    public static final float MAX_PRESSURE_ELEVATOR = MAX_PRESSURE_TIER_ONE;
    public static final float MAX_PRESSURE_VACUUM_PUMP = MAX_PRESSURE_TIER_ONE;
    public static final float MAX_PRESSURE_PNEUMATIC_DOOR = MAX_PRESSURE_TIER_ONE;
    public static final float MAX_PRESSURE_ASSEMBLY_CONTROLLER = MAX_PRESSURE_TIER_ONE;
    public static final float MAX_PRESSURE_UV_LIGHTBOX = MAX_PRESSURE_TIER_ONE;
    public static final float MAX_PRESSURE_UNIVERSAL_SENSOR = MAX_PRESSURE_TIER_ONE;
    public static final float MAX_PRESSURE_THERMAL_COMPRESSOR = MAX_PRESSURE_TIER_ONE;

    public static final float MAX_PRESSURE_TIER_TWO = 25F;
    public static final float MAX_PRESSURE_ADVANCED_PRESSURE_TUBE = MAX_PRESSURE_TIER_TWO;
    public static final float MAX_PRESSURE_CHARGING_STATION = MAX_PRESSURE_TIER_TWO;
//    public static final float MAX_PRESSURE_PNEUMATIC_GENERATOR = MAX_PRESSURE_TIER_TWO;
//    public static final float MAX_PRESSURE_ELECTRIC_COMPRESSOR = MAX_PRESSURE_TIER_TWO;
//    public static final float MAX_PRESSURE_PNEUMATIC_ENGINE = MAX_PRESSURE_TIER_TWO;
//    public static final float MAX_PRESSURE_KINETIC_COMPRESSOR = MAX_PRESSURE_TIER_TWO;
    public static final float MAX_PRESSURE_PNEUMATIC_DYNAMO = MAX_PRESSURE_TIER_TWO;
    public static final float MAX_PRESSURE_FLUX_COMPRESSOR = MAX_PRESSURE_TIER_TWO;
    public static final float MAX_PRESSURE_SOLAR_COMPRESSOR = MAX_PRESSURE_TIER_TWO;
    public static final float MAX_PRESSURE_AERIAL_INTERFACE = MAX_PRESSURE_TIER_TWO;
    public static final float MAX_PRESSURE_ELECTROSTATIC_COMPRESSOR = MAX_PRESSURE_TIER_TWO;
//    public static final float MAX_PRESSURE_PNEUMATIC_PUMP = MAX_PRESSURE_TIER_TWO;

    public static final float MAX_PRESSURE_LIVING_ENTITY = 1.0F;

    public static final int AIR_LEAK_FACTOR = 40;//mL/bar/tick determines how much air being released.

    public static final int CHARGING_STATION_CHARGE_RATE = 10;// mL per tick
    public static final int USAGE_AIR_GRATE = 2; // mL per entity affected this tick
    public static final int USAGE_VORTEX_CANNON = 250; // mL per shot
    public static final int USAGE_ELEVATOR = 300; // mL per meter ascending.
    public static final int USAGE_ENTITY_TRACKER = 1; // mL per tick
    public static final int USAGE_BLOCK_TRACKER = 1; // mL per tick
    public static final int USAGE_COORD_TRACKER = 1; // mL per tick
    public static final int USAGE_ITEM_SEARCHER = 1;
    public static final int USAGE_CHAMBER_INTERFACE = 1000;// mL per item transfered.
    public static final int USAGE_VACUUM_PUMP = 10;// mL per tick
    public static final int USAGE_PNEUMATIC_DOOR = 300;
    public static final int USAGE_ASSEMBLING = 2; //mL/tick
    public static final int USAGE_UV_LIGHTBOX = 2; //mL per tick the UV light is on.
    public static final int USAGE_UNIVERSAL_SENSOR = 1;//mL per tick.
    public static final int USAGE_AERIAL_INTERFACE = 1; //mL per Tick;
    public static final int USAGE_PNEUMATIC_WRENCH = 50;//mL per usage.
    public static final int USAGE_LOGISTICS_CONFIGURATOR = 50;//mL per usage.
    public static final int USAGE_CAMO_APPLICATOR = 50;//mL per usage.
    public static final int USAGE_PROGRAMMABLE_CONTROLLER = 10;//mL per tick when not idling
    public static final int USAGE_PROGRAMMABLE_CONTROLLER_CHUNKLOAD_SELF = 10;//mL per tick to load its own chunk
    public static final int USAGE_PROGRAMMABLE_CONTROLLER_CHUNKLOAD_WORK = 10;//mL per tick to load the work area chunk
    public static final int USAGE_PROGRAMMABLE_CONTROLLER_CHUNKLOAD_WORK3 = 30;//mL per tick to load 3x3 chunks around the work area
    public static final int USAGE_ITEM_MINIGUN = 20;//mL per tick while firing
    public static final int USAGE_ITEM_MANOMETER = 30;//mL per usage
    public static final int USAGE_JACKHAMMER = 50; // mL per block broken
    public static final int USAGE_VACUUM_TRAP = 10; // mL per mob absorbed per point of mob health
    public static final int USAGE_SPAWNER_EXTRACTOR = 1; // mL per tick added while extractor is running
    public static final int USAGE_PRESSURIZED_SPAWNER = 150; // mL per spawn attempt (even if unsuccessful)

    public static final int PRODUCTION_COMPRESSOR = 10; // mL per tick
    public static final int PRODUCTION_ADVANCED_COMPRESSOR = 50; // mL per tick
    public static final int PRODUCTION_SOLAR_COMPRESSOR = 2; // mL per tick at ambient temperature
    public static final int PRODUCTION_VACUUM_PUMP = 4;// mL vacuum per tick
//    public static final int PRODUCTION_PNEUMATIC_ENGINE = 100; //MJ/pump move.
    public static final int PRODUCTION_ELECTROSTATIC_COMPRESSOR = 200000; //per lightning strike

    // volumes (mL)
    public static final int VOLUME_AIR_COMPRESSOR = 5000;
    public static final int VOLUME_ADVANCED_AIR_COMPRESSOR = 10000;
    public static final int VOLUME_MANUAL_COMPRESSOR = 5000;
    public static final int VOLUME_AIR_CANNON = 2000;
    public static final int VOLUME_PRESSURE_TUBE = 1000;
    public static final int VOLUME_CHARGING_STATION = 1000;
    public static final int VOLUME_ELEVATOR = 10000;
    public static final int VOLUME_VACUUM_PUMP = 2000;
    public static final int VOLUME_PNEUMATIC_DOOR = 2000;
    public static final int VOLUME_ASSEMBLY_CONTROLLER = 2000;
    public static final int VOLUME_UV_LIGHTBOX = 2000;
    public static final int VOLUME_UNIVERSAL_SENSOR = 5000;
    public static final int VOLUME_ADVANCED_PRESSURE_TUBE = 4000;
//    public static final int VOLUME_PNEUMATIC_GENERATOR = 10000;
//    public static final int VOLUME_ELECTRIC_COMPRESSOR = 10000;
//    public static final int VOLUME_PNEUMATIC_ENGINE = 10000;
//    public static final int VOLUME_KINETIC_COMPRESSOR = 10000;
    public static final int VOLUME_PNEUMATIC_DYNAMO = 10000;
    public static final int VOLUME_FLUX_COMPRESSOR = 10000;
    public static final int VOLUME_SOLAR_COMPRESSOR = 10000;
    public static final int VOLUME_AERIAL_INTERFACE = 4000;
    public static final int VOLUME_ELECTROSTATIC_COMPRESSOR = 50000;
//    public static final int VOLUME_PNEUMATIC_PUMP = 10000;
    public static final int VOLUME_THERMAL_COMPRESSOR = 5000;
    public static final int VOLUME_FLUID_MIXER = 3000;
    public static final int VOLUME_JACKHAMMER = 12000;
    public static final int VOLUME_VACUUM_TRAP = 10000;
    public static final int VOLUME_SPAWNER_EXTRACTOR = 4000;
    public static final int VOLUME_PRESSURIZED_SPAWNER = 5000;

    public static final int VOLUME_PRESSURE_CHAMBER_PER_EMPTY = 16000;

    // min working pressures (bar)
    public static final float MIN_PRESSURE_AIR_CANNON = 2F;
    public static final float MIN_PRESSURE_ELEVATOR = 3F;
    public static final float MIN_PRESSURE_VACUUM_PUMP = 2F;
    public static final float MIN_PRESSURE_PNEUMATIC_DOOR = 2F;
    public static final float MIN_PRESSURE_ASSEMBLY_CONTROLLER = 3.5F;
    public static final float MIN_PRESSURE_UV_LIGHTBOX = 1.0F;
    public static final float MIN_PRESSURE_UNIVERSAL_SENSOR = 0.5F;
//    public static final float MIN_PRESSURE_PNEUMATIC_GENERATOR = 15F;
//    public static final float MIN_PRESSURE_PNEUMATIC_ENGINE = 5F;
    public static final float MIN_PRESSURE_PNEUMATIC_DYNAMO = 15F;
//    public static final float MIN_PRESSURE_PNEUMATIC_PUMP = 5F;
    public static final float MIN_PRESSURE_AERIAL_INTERFACE = 10F;

    public static final int MAX_REDIRECTION_PER_IRON_BAR = 10000; //mL/lightning bolt/bar

    public static final int AIR_CANISTER_MAX_AIR = 30000;
    public static final int REINFORCED_AIR_CANISTER_MAX_AIR = 240000;
    public static final int VORTEX_CANNON_MAX_AIR = 30000;

    public static final int AIR_CANISTER_VOLUME = 3000;
    public static final int REINFORCED_AIR_CANISTER_VOLUME = 12000;
    public static final int VORTEX_CANNON_VOLUME = 3000;
    public static final int PNEUMATIC_HELMET_VOLUME = 12000;
    public static final int PNEUMATIC_CHESTPLATE_VOLUME = 28000;
    public static final int PNEUMATIC_LEGGINGS_VOLUME = 12000;
    public static final int PNEUMATIC_BOOTS_VOLUME = 12000;

    public static final int PNEUMATIC_WRENCH_VOLUME = 3000;
    public static final int PNEUMATIC_WRENCH_MAX_AIR = 30000;

    public static final int DRONE_VOLUME = 12000;
    public static final float DRONE_MAX_PRESSURE = 10F;
    public static final float DRONE_LOW_PRESSURE = 1F; //pressure at which the drone will start to search for a charging pad.
//    public static final int DRONE_USAGE_DIG = 200;//per block, Based on BC quarry, 60MJ/block.
    public static final int DRONE_USAGE_PLACE = 100;//per block, Based on BC filler, 25MJ/block.
    public static final int DRONE_USAGE_INV = 10;//per stack
    public static final int DRONE_USAGE_ATTACK = 200;//per hit
    public static final int DRONE_USAGE_VOID = 1;//per item
    public static final int DRONE_USAGE_TELEPORT = 10000;//air cost to teleport
    public static final int DRONE_USAGE_CHUNKLOAD = 5;//per tick per upgrade

    public static final float DEF_SPEED_UPGRADE_MULTIPLIER = 1.5F;
    public static final float DEF_SPEED_UPGRADE_USAGE_MULTIPLIER = 1.65F;

    public static final int RANGE_UPGRADE_HELMET_RANGE_INCREASE = 5;
    public static final int NORMAL_TANK_CAPACITY = 16000;
    public static final int MOLTEN_PLASTIC_TEMPERATURE = 150 + 273;//150 C
    public static final int DRONE_TANK_SIZE = 16000;

    public static final int MAGNET_AIR_USAGE = 20; // mL per stack pulled
    public static final int MAGNET_BASE_RANGE = 4;
    public static final int MAGNET_MAX_UPGRADES = 6;
    public static final int PNEUMATIC_ARMOR_DURABILITY_BASE = 24; // halfway between iron and diamond
    public static final int PNEUMATIC_ARMOR_REPAIR_USAGE = 100; // mL per durability repaired
    public static final int PNEUMATIC_ARMOR_FALL_USAGE = 20; // mL per block fallen
    public static final int ARMOR_CHARGER_INTERVAL = 20;  // ticks
    public static final int ARMOR_CHARGING_MAX_UPGRADES = 6;
    public static final int ARMOR_REPAIR_MAX_UPGRADES = 5;
    public static final int PNEUMATIC_LEGS_SPEED_USAGE = 1; // every tick the player is moving and on ground
    public static final int PNEUMATIC_LEGS_MAX_SPEED = 4;
    public static final float PNEUMATIC_LEGS_BOOST_PER_UPGRADE = 0.035f;
    public static final int PNEUMATIC_ARMOR_JUMP_USAGE = 25;
    public static final int PNEUMATIC_ARMOR_FIRE_USAGE = 50;
    public static final int PNEUMATIC_KICK_AIR_USAGE = 150;
    public static final int PNEUMATIC_KICK_MAX_UPGRADES = 4;
    public static final int PNEUMATIC_LEGS_MAX_JUMP = 4;
    public static final int PNEUMATIC_JET_BOOTS_USAGE = 12;  // every tick the boots are firing, per upgrade
    public static final int PNEUMATIC_JET_BOOTS_MAX_UPGRADES = 5;
    public static final int PNEUMATIC_LAUNCHER_MAX_UPGRADES = 4;
    public static final int PNEUMATIC_NIGHT_VISION_USAGE = 1;
    public static final int PNEUMATIC_HELMET_SCUBA_MULTIPLIER = 8;

}
