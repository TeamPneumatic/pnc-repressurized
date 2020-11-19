package me.desht.pneumaticcraft.common.pneumatic_armor;

import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.*;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public enum ArmorUpgradeRegistry {
    INSTANCE;

    /**
     * Used for translation keys and keybind naming
     */
    private static final String UPGRADE_PREFIX = "pneumaticcraft.armor.upgrade.";

    private final List<List<IArmorUpgradeHandler>> upgradeHandlers;

    public static final EquipmentSlotType[] ARMOR_SLOTS = new EquipmentSlotType[] {
            EquipmentSlotType.HEAD,
            EquipmentSlotType.CHEST,
            EquipmentSlotType.LEGS,
            EquipmentSlotType.FEET
    };

    public final IArmorUpgradeHandler coreComponentsHandler;
    public final IArmorUpgradeHandler blockTrackerHandler;
    public final IArmorUpgradeHandler entityTrackerHandler;
    public final IArmorUpgradeHandler searchHandler;
    public final IArmorUpgradeHandler coordTrackerHandler;
    public final IArmorUpgradeHandler droneDebugHandler;
    public final IArmorUpgradeHandler nightVisionHandler;
    public final IArmorUpgradeHandler scubaHandler;
    public final IArmorUpgradeHandler hackHandler;

    public final IArmorUpgradeHandler magnetHandler;
    public final IArmorUpgradeHandler chargingHandler;
    public final IArmorUpgradeHandler chestplateLauncherHandler;
    public final IArmorUpgradeHandler airConHandler;
    public final IArmorUpgradeHandler reachDistanceHandler;

    public final IArmorUpgradeHandler runSpeedHandler;
    public final IArmorUpgradeHandler jumpBoostHandler;

    public final IArmorUpgradeHandler jetBootsHandler;
    public final IArmorUpgradeHandler stepAssistHandler;
    public final IArmorUpgradeHandler kickHandler;

    public static ArmorUpgradeRegistry getInstance() {
        return INSTANCE;
    }

    ArmorUpgradeRegistry() {
        upgradeHandlers = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            upgradeHandlers.add(new ArrayList<>());
        }

        coreComponentsHandler = registerUpgradeHandler(new CoreComponentsHandler());
        blockTrackerHandler = registerUpgradeHandler(new BlockTrackerHandler());
        entityTrackerHandler = registerUpgradeHandler(new EntityTrackerHandler());
        searchHandler = registerUpgradeHandler(new SearchHandler());
        coordTrackerHandler = registerUpgradeHandler(new CoordTrackerHandler());
        droneDebugHandler = registerUpgradeHandler(new DroneDebugHandler());
        nightVisionHandler = registerUpgradeHandler(new NightVisionHandler());
        scubaHandler = registerUpgradeHandler(new ScubaHandler());
        hackHandler = registerUpgradeHandler(new HackHandler());

        magnetHandler = registerUpgradeHandler(new MagnetHandler());
        chargingHandler = registerUpgradeHandler(new ChargingHandler());
        chestplateLauncherHandler = registerUpgradeHandler(new ChestplateLauncherHandler());
        airConHandler = registerUpgradeHandler(new AirConHandler());
        reachDistanceHandler = registerUpgradeHandler(new ReachDistanceHandler());

        runSpeedHandler = registerUpgradeHandler(new SpeedBoostHandler());
        jumpBoostHandler = registerUpgradeHandler(new JumpBoostHandler());

        jetBootsHandler = registerUpgradeHandler(new JetBootsHandler());
        stepAssistHandler = registerUpgradeHandler(new StepAssistHandler());
        kickHandler = registerUpgradeHandler(new KickHandler());
    }

    public static void init() {
    }

    /**
     * Source of truth for all translation keys and keybind names. Standard prefix, followed by a resource location ID,
     * where the ID is converted to a string.  ID's from the "pneumaticcraft" namespace use just the resource location's
     * path, while ID's from other namespaces include the namespace. E.g.:
     * <ul>
     * <li>"pneumaticcraft:block_tracker" -> "pneumaticcraft.armor.upgrade.block_tracker"</li>
     * <li>"pneumaticcraft:block_tracker.module.energy" -> "pneumaticcraft.armor.upgrade.block_tracker.module.energy"</li>
     * <li>"mod2:other_upgrade" -> "pneumaticcraft.armor.upgrade.mod2.other_upgrade"</li>
     * </ul>
     * @param id the ID to convert
     * @return a converted string
     */
    public static String getStringKey(ResourceLocation id) {
        return UPGRADE_PREFIX +
                (id.getNamespace().equals(Names.MOD_ID) ? id.getPath() : id.toString().replace(':', '.'));
    }

    IArmorUpgradeHandler registerUpgradeHandler(IArmorUpgradeHandler handler) {
        upgradeHandlers.get(handler.getEquipmentSlot().getIndex()).add(handler);
        return handler;
    }

    public List<IArmorUpgradeHandler> getHandlersForSlot(EquipmentSlotType slotType) {
        return upgradeHandlers.get(slotType.getIndex());
    }
}
