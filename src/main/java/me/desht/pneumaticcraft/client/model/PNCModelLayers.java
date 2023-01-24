package me.desht.pneumaticcraft.client.model;

import net.minecraft.client.model.geom.ModelLayerLocation;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class PNCModelLayers {
    // pneumatic armor
    public static final ModelLayerLocation PNEUMATIC_LEGS = register("leggings");
    public static final ModelLayerLocation PNEUMATIC_ARMOR = register("armor");

    // entities
    public static final ModelLayerLocation HEAT_FRAME = register("heat_frame");
    public static final ModelLayerLocation LOGISTICS_FRAME = register("logistics_frame");
    public static final ModelLayerLocation SPAWNER_AGITATOR = register("spawner_agitator");
    public static final ModelLayerLocation TRANSFER_GADGET = register("transfer_gadget");
    public static final ModelLayerLocation CROP_SUPPORT = register("crop_support");

    public static final ModelLayerLocation MINIGUN = register("minigun");

    public static final ModelLayerLocation DRONE = register("drone");
    public static final ModelLayerLocation DRONE_CORE = register("drone", "core");

    // block entities
    public static final ModelLayerLocation AIR_CANNON = register("air_cannon");
    public static final ModelLayerLocation PNEUMATIC_DOOR = register("pneumatic_door");
    public static final ModelLayerLocation PNEUMATIC_DOOR_BASE = register("pneumatic_door_base");
    public static final ModelLayerLocation ASSEMBLY_CONTROLLER = register("assembly_controller");
    public static final ModelLayerLocation ASSEMBLY_DRILL = register("assembly_drill");
    public static final ModelLayerLocation ASSEMBLY_LASER = register("assembly_laser");
    public static final ModelLayerLocation ASSEMBLY_IO_UNIT = register("assembly_io_unit");
    public static final ModelLayerLocation ASSEMBLY_PLATFORM = register("assembly_platform");
    public static final ModelLayerLocation ELEVATOR_BASE = register("elevator_base");
    public static final ModelLayerLocation PRESSURE_CHAMBER_INTERFACE = register("pressure_chamber_interface");
    public static final ModelLayerLocation SPAWNER_EXTRACTOR = register("spawner_extractor");
    public static final ModelLayerLocation UNIVERSAL_SENSOR = register("universal_sensor");
    public static final ModelLayerLocation VACUUM_PUMP = register("vacuum_pump");
    public static final ModelLayerLocation SOLAR_COMPRESSOR = register("solar_compressor");

    // tube modules
    public static final ModelLayerLocation AIR_GRATE_MODULE = register("air_grate_module");
    public static final ModelLayerLocation CHARGING_MODULE = register("charging_module");
    public static final ModelLayerLocation FLOW_DETECTOR_MODULE = register("flow_detector_module");
    public static final ModelLayerLocation LOGISTICS_MODULE = register("logistics_module");
    public static final ModelLayerLocation PRESSURE_GAUGE_MODULE = register("pressure_gauge_module");
    public static final ModelLayerLocation REDSTONE_MODULE = register("redstone_module");
    public static final ModelLayerLocation REGULATOR_MODULE = register("regulator_module");
    public static final ModelLayerLocation SAFETY_VALVE_MODULE = register("safety_valve_module");
    public static final ModelLayerLocation VACUUM_MODULE = register("vacuum_module");

    // =====================================================================

    private static ModelLayerLocation register(String name) {
        return register(name, "main");
    }

    private static ModelLayerLocation register(String name, String part) {
        return new ModelLayerLocation(RL(name), part);
    }
}
