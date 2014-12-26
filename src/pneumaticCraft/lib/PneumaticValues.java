package pneumaticCraft.lib;

public class PneumaticValues{

    // danger pressures (bar)
    public static final float DANGER_PRESSURE_TIER_ONE = 5;
    public static final float DANGER_PRESSURE_PRESSURE_TUBE = DANGER_PRESSURE_TIER_ONE;
    public static final float DANGER_PRESSURE_AIR_COMPRESSOR = DANGER_PRESSURE_TIER_ONE;
    public static final float DANGER_PRESSURE_AIR_CANNON = DANGER_PRESSURE_TIER_ONE;
    public static final float DANGER_PRESSURE_PRESSURE_CHAMBER = DANGER_PRESSURE_TIER_ONE;
    public static final float DANGER_PRESSURE_ELEVATOR = DANGER_PRESSURE_TIER_ONE;
    public static final float DANGER_PRESSURE_VACUUM_PUMP = DANGER_PRESSURE_TIER_ONE;
    public static final float DANGER_PRESSURE_PNEUMATIC_DOOR = DANGER_PRESSURE_TIER_ONE;
    public static final float DANGER_PRESSURE_ASSEMBLY_CONTROLLER = DANGER_PRESSURE_TIER_ONE;
    public static final float DANGER_PRESSURE_UV_LIGHTBOX = DANGER_PRESSURE_TIER_ONE;
    public static final float DANGER_PRESSURE_UNIVERSAL_SENSOR = DANGER_PRESSURE_TIER_ONE;

    public static final float DANGER_PRESSURE_TIER_TWO = 20;
    public static final float DANGER_PRESSURE_ADVANCED_PRESSURE_TUBE = DANGER_PRESSURE_TIER_TWO;
    public static final float DANGER_PRESSURE_CHARGING_STATION = DANGER_PRESSURE_TIER_TWO;
    public static final float DANGER_PRESSURE_PNEUMATIC_GENERATOR = DANGER_PRESSURE_TIER_TWO;
    public static final float DANGER_PRESSURE_ELECTRIC_COMPRESSOR = DANGER_PRESSURE_TIER_TWO;
    public static final float DANGER_PRESSURE_PNEUMATIC_ENGINE = DANGER_PRESSURE_TIER_TWO;
    public static final float DANGER_PRESSURE_KINETIC_COMPRESSOR = DANGER_PRESSURE_TIER_TWO;
    public static final float DANGER_PRESSURE_PNEUMATIC_DYNAMO = DANGER_PRESSURE_TIER_TWO;
    public static final float DANGER_PRESSURE_FLUX_COMPRESSOR = DANGER_PRESSURE_TIER_TWO;
    public static final float DANGER_PRESSURE_AERIAL_INTERFACE = DANGER_PRESSURE_TIER_TWO;
    public static final float DANGER_PRESSURE_ELECTROSTATIC_COMPRESSOR = DANGER_PRESSURE_TIER_TWO;
    public static final float DANGER_PRESSURE_PNEUMATIC_PUMP = DANGER_PRESSURE_TIER_TWO;

    // critical pressures (bar)
    public static final float MAX_PRESSURE_TIER_ONE = 7F;
    public static final float MAX_PRESSURE_PRESSURE_TUBE = MAX_PRESSURE_TIER_ONE;
    public static final float MAX_PRESSURE_AIR_COMPRESSOR = MAX_PRESSURE_TIER_ONE;
    public static final float MAX_PRESSURE_AIR_CANNON = MAX_PRESSURE_TIER_ONE;
    public static final float MAX_PRESSURE_PRESSURE_CHAMBER = MAX_PRESSURE_TIER_ONE;
    public static final float MAX_PRESSURE_ELEVATOR = MAX_PRESSURE_TIER_ONE;
    public static final float MAX_PRESSURE_VACUUM_PUMP = MAX_PRESSURE_TIER_ONE;
    public static final float MAX_PRESSURE_PNEUMATIC_DOOR = MAX_PRESSURE_TIER_ONE;
    public static final float MAX_PRESSURE_ASSEMBLY_CONTROLLER = MAX_PRESSURE_TIER_ONE;
    public static final float MAX_PRESSURE_UV_LIGHTBOX = MAX_PRESSURE_TIER_ONE;
    public static final float MAX_PRESSURE_UNIVERSAL_SENSOR = MAX_PRESSURE_TIER_ONE;

    public static final float MAX_PRESSURE_TIER_TWO = 25F;
    public static final float MAX_PRESSURE_ADVANCED_PRESSURE_TUBE = MAX_PRESSURE_TIER_TWO;
    public static final float MAX_PRESSURE_CHARGING_STATION = MAX_PRESSURE_TIER_TWO;
    public static final float MAX_PRESSURE_PNEUMATIC_GENERATOR = MAX_PRESSURE_TIER_TWO;
    public static final float MAX_PRESSURE_ELECTRIC_COMPRESSOR = MAX_PRESSURE_TIER_TWO;
    public static final float MAX_PRESSURE_PNEUMATIC_ENGINE = MAX_PRESSURE_TIER_TWO;
    public static final float MAX_PRESSURE_KINETIC_COMPRESSOR = MAX_PRESSURE_TIER_TWO;
    public static final float MAX_PRESSURE_PNEUMATIC_DYNAMO = MAX_PRESSURE_TIER_TWO;
    public static final float MAX_PRESSURE_FLUX_COMPRESSOR = MAX_PRESSURE_TIER_TWO;
    public static final float MAX_PRESSURE_AERIAL_INTERFACE = MAX_PRESSURE_TIER_TWO;
    public static final float MAX_PRESSURE_ELECTROSTATIC_COMPRESSOR = MAX_PRESSURE_TIER_TWO;
    public static final float MAX_PRESSURE_PNEUMATIC_PUMP = MAX_PRESSURE_TIER_TWO;

    public static final float MAX_PRESSURE_LIVING_ENTITY = 1.0F;

    public static final int AIR_LEAK_FACTOR = 40;//mL/bar/tick determines how much air being released.

    public static final int CHARGING_STATION_CHARGE_RATE = 10;// mL per tick
    public static final int USAGE_AIR_GRATE = 10; // mL per tick per meter range
    public static final int USAGE_VORTEX_CANNON = 500; // mL per shot
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
    public static final int USAGE_PNEUMATIC_WRENCH = 100;//mL per usage.

    public static final int PRODUCTION_COMPRESSOR = 10; // mL per tick
    public static final int PRODUCTION_ADVANCED_COMPRESSOR = 50; // mL per tick
    public static final int PRODUCTION_VACUUM_PUMP = 2;// mL vacuum per tick
    public static final int PRODUCTION_PNEUMATIC_ENGINE = 100; //MJ/pump move.
    public static final int PRODUCTION_ELECTROSTATIC_COMPRESSOR = 200000; //per lightning strike

    // volumes (mL)
    public static final int VOLUME_AIR_COMPRESSOR = 5000;
    public static final int VOLUME_AIR_CANNON = 2000;
    public static final int VOLUME_PRESSURE_TUBE = 1000;
    public static final int VOLUME_PRESSURE_CHAMBER = 1000;
    public static final int VOLUME_CHARGING_STATION = 1000;
    public static final int VOLUME_ELEVATOR = 10000;
    public static final int VOLUME_VACUUM_PUMP = 2000;
    public static final int VOLUME_PNEUMATIC_DOOR = 2000;
    public static final int VOLUME_ASSEMBLY_CONTROLLER = 2000;
    public static final int VOLUME_UV_LIGHTBOX = 2000;
    public static final int VOLUME_UNIVERSAL_SENSOR = 5000;
    public static final int VOLUME_ADVANCED_PRESSURE_TUBE = 4000;
    public static final int VOLUME_PNEUMATIC_GENERATOR = 10000;
    public static final int VOLUME_ELECTRIC_COMPRESSOR = 10000;
    public static final int VOLUME_PNEUMATIC_ENGINE = 10000;
    public static final int VOLUME_KINETIC_COMPRESSOR = 10000;
    public static final int VOLUME_PNEUMATIC_DYNAMO = 10000;
    public static final int VOLUME_FLUX_COMPRESSOR = 10000;
    public static final int VOLUME_AERIAL_INTERFACE = 4000;
    public static final int VOLUME_ELECTROSTATIC_COMPRESSOR = 50000;
    public static final int VOLUME_PNEUMATIC_PUMP = 10000;

    public static final int VOLUME_PRESSURE_CHAMBER_PER_EMPTY = 16000;
    public static final int VOLUME_VOLUME_UPGRADE = 5000;

    // min working pressures (bar)
    public static final float MIN_PRESSURE_AIR_CANNON = 2F;
    public static final float MIN_PRESSURE_ELEVATOR = 3F;
    public static final float MIN_PRESSURE_VACUUM_PUMP = 2F;
    public static final float MIN_PRESSURE_PNEUMATIC_DOOR = 2F;
    public static final float MIN_PRESSURE_ASSEMBLY_CONTROLLER = 3.5F;
    public static final float MIN_PRESSURE_UV_LIGHTBOX = 1.0F;
    public static final float MIN_PRESSURE_UNIVERSAL_SENSOR = 0.5F;
    public static final float MIN_PRESSURE_PNEUMATIC_GENERATOR = 15F;
    public static final float MIN_PRESSURE_PNEUMATIC_ENGINE = 5F;
    public static final float MIN_PRESSURE_PNEUMATIC_DYNAMO = 15F;
    public static final float MIN_PRESSURE_PNEUMATIC_PUMP = 5F;
    public static final float MIN_PRESSURE_AERIAL_INTERFACE = 10F;

    public static final int MAX_REDIRECTION_PER_IRON_BAR = 10000; //mL/lightning bolt/bar

    public static final int AIR_CANISTER_MAX_AIR = 30000;
    public static final int VORTEX_CANNON_MAX_AIR = 30000;
    public static final int PNEUMATIC_HELMET_MAX_AIR = 120000;

    public static final int AIR_CANISTER_VOLUME = 3000;
    public static final int VORTEX_CANNON_VOLUME = 3000;
    public static final int PNEUMATIC_HELMET_VOLUME = 12000;

    public static final int PNEUMATIC_WRENCH_VOLUME = 3000;
    public static final int PNEUMATIC_WRENCH_MAX_AIR = 30000;

    public static final int DRONE_VOLUME = 12000;
    public static final float DRONE_MAX_PRESSURE = 10F;
    public static final float DRONE_LOW_PRESSURE = 1F; //pressure at which the drone will start to search for a charging pad.
    public static final int DRONE_USAGE_DIG = 200;//per block, Based on BC quarry, 60MJ/block.
    public static final int DRONE_USAGE_PLACE = 100;//per block, Based on BC filler, 25MJ/block.
    public static final int DRONE_USAGE_INV = 10;//per stack
    public static final int DRONE_USAGE_ATTACK = 200;//per hit

    public static final float SPEED_UPGRADE_MULTIPLIER = 1.5F;
    public static final float SPEED_UPGRADE_USAGE_MULTIPLIER = 1.8F;

    public static final int RANGE_UPGRADE_HELMET_RANGE_INCREASE = 5;
    public static final double PLASTIC_MIX_RATIO = 0.2;
    public static final int NORMAL_TANK_CAPACITY = 16000;
    public static final int PLASTIC_MIXER_HEAT_RATIO = 1;
    public static final int PLASTIC_MIXER_MELTING_TEMP = 150 + 273;//150 C
    public static final int MAX_DRAIN = 100;
    public static final int DRONE_TANK_SIZE = 16000;

}
