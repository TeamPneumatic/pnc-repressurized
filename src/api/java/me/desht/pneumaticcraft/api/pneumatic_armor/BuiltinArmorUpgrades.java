package me.desht.pneumaticcraft.api.pneumatic_armor;

import net.minecraft.resources.ResourceLocation;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * A collection of the known armor upgrade IDs which PneumaticCraft: Repressurized registers.  You can obtain the
 * corresponding {@link IArmorUpgradeHandler} object from these IDs with
 * {@link ICommonArmorRegistry#getArmorUpgradeHandler(ResourceLocation)}.
 */
public class BuiltinArmorUpgrades {
    public static final ResourceLocation AIR_CONDITIONING = RL("air_conditioning");
    public static final ResourceLocation BLOCK_TRACKER = RL("block_tracker");
    public static final ResourceLocation CHARGING = RL("charging");
    public static final ResourceLocation CHESTPLATE_LAUNCHER = RL("chestplate_launcher");
    public static final ResourceLocation COORDINATE_TRACKER = RL("coordinate_tracker");
    public static final ResourceLocation CORE_COMPONENTS = RL("core_components");
    public static final ResourceLocation DRONE_DEBUG = RL("drone_debug");
    public static final ResourceLocation ELYTRA = RL("elytra");
    public static final ResourceLocation ENDER_VISOR = RL("ender_visor");
    public static final ResourceLocation ENTITY_TRACKER = RL("entity_tracker");
    public static final ResourceLocation FALL_PROTECTION = RL("fall_protection");
    public static final ResourceLocation HACKING = RL("hacking");
    public static final ResourceLocation JET_BOOTS = RL("jet_boots");
    public static final ResourceLocation JUMP_BOOST = RL("jump_boost");
    public static final ResourceLocation KICK = RL("kick");
    public static final ResourceLocation MAGNET = RL("magnet");
    public static final ResourceLocation NIGHT_VISION = RL("night_vision");
    public static final ResourceLocation REACH_DISTANCE = RL("reach_distance");
    public static final ResourceLocation RUN_SPEED = RL("run_speed");
    public static final ResourceLocation SCUBA = RL("scuba");
    public static final ResourceLocation SEARCH = RL("search");
    public static final ResourceLocation STEP_ASSIST = RL("step_assist");
    public static final ResourceLocation STOMP = RL("stomp");
}
