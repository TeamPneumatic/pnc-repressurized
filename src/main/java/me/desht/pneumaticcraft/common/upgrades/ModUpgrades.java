package me.desht.pneumaticcraft.common.upgrades;

import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModUpgrades {
    private static final Map<BuiltinUpgrade, PNCUpgrade> BUILTIN_MAP = new EnumMap<>(BuiltinUpgrade.class);

    public static final Supplier<PNCUpgrade> VOLUME = () -> BUILTIN_MAP.get(BuiltinUpgrade.VOLUME);
    public static final Supplier<PNCUpgrade> DISPENSER = () -> BUILTIN_MAP.get(BuiltinUpgrade.DISPENSER);
    public static final Supplier<PNCUpgrade> ITEM_LIFE = () -> BUILTIN_MAP.get(BuiltinUpgrade.ITEM_LIFE);
    public static final Supplier<PNCUpgrade> ENTITY_TRACKER = () -> BUILTIN_MAP.get(BuiltinUpgrade.ENTITY_TRACKER);
    public static final Supplier<PNCUpgrade> BLOCK_TRACKER = () -> BUILTIN_MAP.get(BuiltinUpgrade.BLOCK_TRACKER);
    public static final Supplier<PNCUpgrade> SPEED = () -> BUILTIN_MAP.get(BuiltinUpgrade.SPEED);
    public static final Supplier<PNCUpgrade> SEARCH = () -> BUILTIN_MAP.get(BuiltinUpgrade.SEARCH);
    public static final Supplier<PNCUpgrade> COORDINATE_TRACKER = () -> BUILTIN_MAP.get(BuiltinUpgrade.COORDINATE_TRACKER);
    public static final Supplier<PNCUpgrade> RANGE = () -> BUILTIN_MAP.get(BuiltinUpgrade.RANGE);
    public static final Supplier<PNCUpgrade> SECURITY = () -> BUILTIN_MAP.get(BuiltinUpgrade.SECURITY);
    public static final Supplier<PNCUpgrade> MAGNET = () -> BUILTIN_MAP.get(BuiltinUpgrade.MAGNET);
    public static final Supplier<PNCUpgrade> THAUMCRAFT = () -> BUILTIN_MAP.get(BuiltinUpgrade.THAUMCRAFT);
    public static final Supplier<PNCUpgrade> CHARGING = () -> BUILTIN_MAP.get(BuiltinUpgrade.CHARGING);
    public static final Supplier<PNCUpgrade> ARMOR = () -> BUILTIN_MAP.get(BuiltinUpgrade.ARMOR);
    public static final Supplier<PNCUpgrade> JET_BOOTS = () -> BUILTIN_MAP.get(BuiltinUpgrade.JET_BOOTS);
    public static final Supplier<PNCUpgrade> NIGHT_VISION = () -> BUILTIN_MAP.get(BuiltinUpgrade.NIGHT_VISION);
    public static final Supplier<PNCUpgrade> SCUBA = () -> BUILTIN_MAP.get(BuiltinUpgrade.SCUBA);
    public static final Supplier<PNCUpgrade> CREATIVE = () -> BUILTIN_MAP.get(BuiltinUpgrade.CREATIVE);
    public static final Supplier<PNCUpgrade> AIR_CONDITIONING = () -> BUILTIN_MAP.get(BuiltinUpgrade.AIR_CONDITIONING);
    public static final Supplier<PNCUpgrade> INVENTORY = () -> BUILTIN_MAP.get(BuiltinUpgrade.INVENTORY);
    public static final Supplier<PNCUpgrade> JUMPING = () -> BUILTIN_MAP.get(BuiltinUpgrade.JUMPING);
    public static final Supplier<PNCUpgrade> FLIPPERS = () -> BUILTIN_MAP.get(BuiltinUpgrade.FLIPPERS);
    public static final Supplier<PNCUpgrade> STANDBY = () -> BUILTIN_MAP.get(BuiltinUpgrade.STANDBY);
    public static final Supplier<PNCUpgrade> MINIGUN = () -> BUILTIN_MAP.get(BuiltinUpgrade.MINIGUN);
    public static final Supplier<PNCUpgrade> RADIATION_SHIELDING = () -> BUILTIN_MAP.get(BuiltinUpgrade.RADIATION_SHIELDING);
    public static final Supplier<PNCUpgrade> GILDED = () -> BUILTIN_MAP.get(BuiltinUpgrade.GILDED);
    public static final Supplier<PNCUpgrade> ENDER_VISOR = () -> BUILTIN_MAP.get(BuiltinUpgrade.ENDER_VISOR);
    public static final Supplier<PNCUpgrade> STOMP = () -> BUILTIN_MAP.get(BuiltinUpgrade.STOMP);
    public static final Supplier<PNCUpgrade> ELYTRA = () -> BUILTIN_MAP.get(BuiltinUpgrade.ELYTRA);
    public static final Supplier<PNCUpgrade> CHUNKLOADER = () -> BUILTIN_MAP.get(BuiltinUpgrade.CHUNKLOADER);

    public static PNCUpgrade registerBuiltin(BuiltinUpgrade bu, PNCUpgrade pncUpgrade) {
        BUILTIN_MAP.put(bu, pncUpgrade);
        return pncUpgrade;
    }
}
