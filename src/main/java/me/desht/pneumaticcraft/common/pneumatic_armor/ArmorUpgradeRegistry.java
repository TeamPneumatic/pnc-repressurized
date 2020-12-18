package me.desht.pneumaticcraft.common.pneumatic_armor;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.*;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public enum ArmorUpgradeRegistry {
    INSTANCE;

    /**
     * Used for translation keys and keybind naming
     */
    private static final String UPGRADE_PREFIX = "pneumaticcraft.armor.upgrade.";

    private final List<List<IArmorUpgradeHandler>> upgradeHandlers;
    private final Object2IntMap<IArmorUpgradeHandler> indexMap;
    private final Map<ResourceLocation, ArmorUpgradeEntry> byID = new HashMap<>();

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
        indexMap = new Object2IntOpenHashMap<>(30);
        indexMap.defaultReturnValue(-1);

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

    public static String getStringKey(ResourceLocation id) {
        return IArmorUpgradeHandler.getStringKey(id);
    }

    IArmorUpgradeHandler registerUpgradeHandler(IArmorUpgradeHandler handler) {
        List<IArmorUpgradeHandler> l = upgradeHandlers.get(handler.getEquipmentSlot().getIndex());
        indexMap.put(handler, l.size());
        byID.put(handler.getID(), new ArmorUpgradeEntry(handler, l.size()));
        l.add(handler);
        return handler;
    }

    public List<IArmorUpgradeHandler> getHandlersForSlot(EquipmentSlotType slotType) {
        return upgradeHandlers.get(slotType.getIndex());
    }

    public int getIndexForHandler(IArmorUpgradeHandler handler) {
        int idx = indexMap.getInt(handler);
        Validate.isTrue(idx >= 0, "unknown handler: " + handler);
        return idx;
    }

    public ArmorUpgradeEntry getUpgradeEntry(ResourceLocation upgradeID) {
        return byID.get(upgradeID);
    }

    public Stream<ArmorUpgradeEntry> entries() {
        return byID.values().stream();
    }

    public static class ArmorUpgradeEntry {
        final IArmorUpgradeHandler handler;
        final int index;

        public ArmorUpgradeEntry(IArmorUpgradeHandler handler, int index) {
            this.handler = handler;
            this.index = index;
        }

        public IArmorUpgradeHandler getHandler() {
            return handler;
        }

        public int getIndex() {
            return index;
        }

        public EquipmentSlotType getSlot() {
            return handler.getEquipmentSlot();
        }
    }
}
