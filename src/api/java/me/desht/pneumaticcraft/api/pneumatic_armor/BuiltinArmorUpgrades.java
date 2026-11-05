package me.desht.pneumaticcraft.api.pneumatic_armor;

import net.minecraft.resources.Identifier;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * A collection of the known armor upgrade IDs which PneumaticCraft: Repressurized registers.  You can obtain the
 * corresponding {@link IArmorUpgradeHandler} object from these IDs with
 * {@link ICommonArmorRegistry#getArmorUpgradeHandler(Identifier)}.
 */
public class BuiltinArmorUpgrades {
    public static final Identifier AIR_CONDITIONING = RL("air_conditioning");
    public static final Identifier BLOCK_TRACKER = RL("block_tracker");
    public static final Identifier CHARGING = RL("charging");
    public static final Identifier CHESTPLATE_LAUNCHER = RL("chestplate_launcher");
    public static final Identifier COORDINATE_TRACKER = RL("coordinate_tracker");
    public static final Identifier CORE_COMPONENTS = RL("core_components");
    public static final Identifier DRONE_DEBUG = RL("drone_debug");
    public static final Identifier ELYTRA = RL("elytra");
    public static final Identifier ENDER_VISOR = RL("ender_visor");
    public static final Identifier ENTITY_TRACKER = RL("entity_tracker");
    public static final Identifier FALL_PROTECTION = RL("fall_protection");
    public static final Identifier HACKING = RL("hacking");
    public static final Identifier JET_BOOTS = RL("jet_boots");
    public static final Identifier JUMP_BOOST = RL("jump_boost");
    public static final Identifier KICK = RL("kick");
    public static final Identifier MAGNET = RL("magnet");
    public static final Identifier NIGHT_VISION = RL("night_vision");
    public static final Identifier REACH_DISTANCE = RL("reach_distance");
    public static final Identifier RUN_SPEED = RL("run_speed");
    public static final Identifier SCUBA = RL("scuba");
    public static final Identifier SEARCH = RL("search");
    public static final Identifier STEP_ASSIST = RL("step_assist");
    public static final Identifier STOMP = RL("stomp");
}
