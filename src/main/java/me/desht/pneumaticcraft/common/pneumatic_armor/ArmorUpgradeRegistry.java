package me.desht.pneumaticcraft.common.pneumatic_armor;

import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.*;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public enum ArmorUpgradeRegistry {
    INSTANCE;

    private final List<List<IArmorUpgradeHandler<?>>> upgradeHandlers;
    private final Map<ResourceLocation, IArmorUpgradeHandler<?>> byID = new HashMap<>();

    public static final EquipmentSlotType[] ARMOR_SLOTS = new EquipmentSlotType[] {
            EquipmentSlotType.HEAD,
            EquipmentSlotType.CHEST,
            EquipmentSlotType.LEGS,
            EquipmentSlotType.FEET
    };

    public final CoreComponentsHandler coreComponentsHandler;
    public final BlockTrackerHandler blockTrackerHandler;
    public final EntityTrackerHandler entityTrackerHandler;
    public final SearchHandler searchHandler;
    public final CoordTrackerHandler coordTrackerHandler;
    public final DroneDebugHandler droneDebugHandler;
    public final NightVisionHandler nightVisionHandler;
    public final ScubaHandler scubaHandler;
    public final HackHandler hackHandler;

    public final MagnetHandler magnetHandler;
    public final ChargingHandler chargingHandler;
    public final ChestplateLauncherHandler chestplateLauncherHandler;
    public final AirConHandler airConHandler;
    public final ReachDistanceHandler reachDistanceHandler;

    public final SpeedBoostHandler runSpeedHandler;
    public final JumpBoostHandler jumpBoostHandler;

    public final JetBootsHandler jetBootsHandler;
    public final StepAssistHandler stepAssistHandler;
    public final KickHandler kickHandler;

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

    public static String getStringKey(ResourceLocation id) {
        return IArmorUpgradeHandler.getStringKey(id);
    }

    <T extends IArmorUpgradeHandler<?>> T registerUpgradeHandler(T handler) {
        List<IArmorUpgradeHandler<?>> l = upgradeHandlers.get(handler.getEquipmentSlot().getIndex());
        handler.setIndex(l.size());
        byID.put(handler.getID(), handler);
        l.add(handler);
        return handler;
    }

    public List<IArmorUpgradeHandler<?>> getHandlersForSlot(EquipmentSlotType slotType) {
        return upgradeHandlers.get(slotType.getIndex());
    }

    public IArmorUpgradeHandler<?> getUpgradeEntry(ResourceLocation upgradeID) {
        if (upgradeID == null) return null;
        return byID.get(upgradeID);
    }

    public Stream<IArmorUpgradeHandler<?>> entries() {
        return byID.values().stream();
    }
}
