package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.item.PNCUpgrade;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ModUpgrades {
    public static final DeferredRegister<PNCUpgrade> UPGRADES_DEFERRED = DeferredRegister.create(RL("upgrades"), Names.MOD_ID);
    public static final Supplier<IForgeRegistry<PNCUpgrade>> UPGRADES = UPGRADES_DEFERRED
            .makeRegistry(() -> new RegistryBuilder<PNCUpgrade>().disableSaving().disableSync());

    public static final RegistryObject<PNCUpgrade> VOLUME = register(BuiltinUpgrade.VOLUME);
    public static final RegistryObject<PNCUpgrade> DISPENSER = register(BuiltinUpgrade.DISPENSER);
    public static final RegistryObject<PNCUpgrade> ITEM_LIFE = register(BuiltinUpgrade.ITEM_LIFE);
    public static final RegistryObject<PNCUpgrade> ENTITY_TRACKER = register(BuiltinUpgrade.ENTITY_TRACKER);
    public static final RegistryObject<PNCUpgrade> BLOCK_TRACKER = register(BuiltinUpgrade.BLOCK_TRACKER);
    public static final RegistryObject<PNCUpgrade> SPEED = register(BuiltinUpgrade.SPEED);
    public static final RegistryObject<PNCUpgrade> SEARCH = register(BuiltinUpgrade.SEARCH);
    public static final RegistryObject<PNCUpgrade> COORDINATE_TRACKER = register(BuiltinUpgrade.COORDINATE_TRACKER);
    public static final RegistryObject<PNCUpgrade> RANGE = register(BuiltinUpgrade.RANGE);
    public static final RegistryObject<PNCUpgrade> SECURITY = register(BuiltinUpgrade.SECURITY);
    public static final RegistryObject<PNCUpgrade> MAGNET = register(BuiltinUpgrade.MAGNET);
    public static final RegistryObject<PNCUpgrade> THAUMCRAFT = register(BuiltinUpgrade.THAUMCRAFT);
    public static final RegistryObject<PNCUpgrade> CHARGING = register(BuiltinUpgrade.CHARGING);
    public static final RegistryObject<PNCUpgrade> ARMOR = register(BuiltinUpgrade.ARMOR);
    public static final RegistryObject<PNCUpgrade> JET_BOOTS = register(BuiltinUpgrade.JET_BOOTS);
    public static final RegistryObject<PNCUpgrade> NIGHT_VISION = register(BuiltinUpgrade.NIGHT_VISION);
    public static final RegistryObject<PNCUpgrade> SCUBA = register(BuiltinUpgrade.SCUBA);
    public static final RegistryObject<PNCUpgrade> CREATIVE = register(BuiltinUpgrade.CREATIVE);
    public static final RegistryObject<PNCUpgrade> AIR_CONDITIONING = register(BuiltinUpgrade.AIR_CONDITIONING);
    public static final RegistryObject<PNCUpgrade> INVENTORY = register(BuiltinUpgrade.INVENTORY);
    public static final RegistryObject<PNCUpgrade> JUMPING = register(BuiltinUpgrade.JUMPING);
    public static final RegistryObject<PNCUpgrade> FLIPPERS = register(BuiltinUpgrade.FLIPPERS);
    public static final RegistryObject<PNCUpgrade> STANDBY = register(BuiltinUpgrade.STANDBY);
    public static final RegistryObject<PNCUpgrade> MINIGUN = register(BuiltinUpgrade.MINIGUN);
    public static final RegistryObject<PNCUpgrade> RADIATION_SHIELDING = register(BuiltinUpgrade.RADIATION_SHIELDING);
    public static final RegistryObject<PNCUpgrade> GILDED = register(BuiltinUpgrade.GILDED);
    public static final RegistryObject<PNCUpgrade> ENDER_VISOR = register(BuiltinUpgrade.ENDER_VISOR);
    public static final RegistryObject<PNCUpgrade> STOMP = register(BuiltinUpgrade.STOMP);
    public static final RegistryObject<PNCUpgrade> ELYTRA = register(BuiltinUpgrade.ELYTRA);
    public static final RegistryObject<PNCUpgrade> CHUNKLOADER = register(BuiltinUpgrade.CHUNKLOADER);

    private static RegistryObject<PNCUpgrade> register(BuiltinUpgrade upgrade) {
        return UPGRADES_DEFERRED.register(upgrade.name, () -> new PNCUpgrade(upgrade.maxTier, upgrade.depModIds));
    }

    enum BuiltinUpgrade {
        VOLUME("volume"),
        DISPENSER("dispenser"),
        ITEM_LIFE("item_life"),
        ENTITY_TRACKER("entity_tracker"),
        BLOCK_TRACKER("block_tracker"),
        SPEED("speed"),
        SEARCH("search"),
        COORDINATE_TRACKER("coordinate_tracker"),
        RANGE("range"),
        SECURITY("security"),
        MAGNET("magnet"),
        THAUMCRAFT("thaumcraft", 1, ModIds.THAUMCRAFT), /*Only around when Thaumcraft is */
        CHARGING("charging"),
        ARMOR("armor"),
        JET_BOOTS("jet_boots", 5),
        NIGHT_VISION("night_vision"),
        SCUBA("scuba"),
        CREATIVE("creative"),
        AIR_CONDITIONING("air_conditioning", 1, ModIds.TOUGH_AS_NAILS),
        INVENTORY("inventory"),
        JUMPING("jumping", 4),
        FLIPPERS("flippers"),
        STANDBY("standby"),
        MINIGUN("minigun"),
        RADIATION_SHIELDING("radiation_shielding", 1, ModIds.MEKANISM),
        GILDED("gilded"),
        ENDER_VISOR("ender_visor"),
        STOMP("stomp"),
        ELYTRA("elytra"),
        CHUNKLOADER("chunkloader");

        private final String name;
        private final int maxTier;
        private final String[] depModIds;

        BuiltinUpgrade(String name) {
            this(name, 1);
        }

        BuiltinUpgrade(String name, int maxTier, String... depModIds) {
            this.name = name;
            this.maxTier = maxTier;
            this.depModIds = depModIds;
        }

        public String getName() {
            return name;
        }

        public int getMaxTier() {
            return maxTier;
        }
    }
}
