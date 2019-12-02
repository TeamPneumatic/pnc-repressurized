package me.desht.pneumaticcraft.lib;

import net.minecraft.util.ResourceLocation;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class Names {
    // Ore dict strings; camel-case is OK here
    public static final String INGOT_IRON_COMPRESSED = "ingotIronCompressed";
    public static final String BLOCK_IRON_COMPRESSED = "blockIronCompressed";
    public static final String GEAR_IRON_COMPRESSED = "gearIronCompressed";
    public static final String DRONE = "pneumaticDrone";
    public static final String GUN_AMMO = "pneumaticGunAmmo";

    public static final ResourceLocation MODULE_SAFETY_VALVE = RL("safety_tube_module");
    public static final ResourceLocation MODULE_REGULATOR = RL("regulator_tube_module");
    public static final ResourceLocation MODULE_GAUGE = RL("pressure_gauge_module");
    public static final ResourceLocation MODULE_FLOW_DETECTOR = RL("flow_detector_module");
    public static final ResourceLocation MODULE_AIR_GRATE = RL("air_grate_module");
    public static final ResourceLocation MODULE_CHARGING = RL("charging_module");
    public static final ResourceLocation MODULE_LOGISTICS = RL("logistics_module");
    public static final ResourceLocation MODULE_REDSTONE = RL("redstone_module");

    public static final String PNEUMATIC_KEYBINDING_CATEGORY = "key.pneumaticcraft.category";

    public static final String MOD_ID = "pneumaticcraft";
    public static final String MOD_NAME = "PneumaticCraft: Repressurized";

    // Agreed by convention among several mods to denote item entities which should not be magnet'd
    public static final String PREVENT_REMOTE_MOVEMENT = "PreventRemoteMovement";

    // Permission nodes
    public static final String AMADRON_ADD_PERIODIC_TRADE = MOD_ID + ".amadron.addPeriodicTrade";
    public static final String AMADRON_ADD_STATIC_TRADE = MOD_ID + ".amadron.addStaticTrade";
}
