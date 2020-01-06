package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.*;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum UpgradeRenderHandlerList {
    INSTANCE;

    private final List<List<IUpgradeRenderHandler>> upgradeRenderers;

    public static UpgradeRenderHandlerList instance() {
        return INSTANCE;
    }

    private final Map<Class<? extends IUpgradeRenderHandler>, IUpgradeRenderHandler> classMap = new HashMap<>();

    // convenience
    public static final EquipmentSlotType[] ARMOR_SLOTS = new EquipmentSlotType[4];
    static {
        ARMOR_SLOTS[0] = EquipmentSlotType.HEAD;
        ARMOR_SLOTS[1] = EquipmentSlotType.CHEST;
        ARMOR_SLOTS[2] = EquipmentSlotType.LEGS;
        ARMOR_SLOTS[3] = EquipmentSlotType.FEET;
    }

    private UpgradeRenderHandlerList() {
        upgradeRenderers = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            upgradeRenderers.add(new ArrayList<>());
        }
        addUpgradeRenderer(new MainHelmetHandler());  // always keep this first
        addUpgradeRenderer(new BlockTrackUpgradeHandler());
        addUpgradeRenderer(new EntityTrackUpgradeHandler());
        addUpgradeRenderer(new SearchUpgradeHandler());
        addUpgradeRenderer(new CoordTrackUpgradeHandler());
        addUpgradeRenderer(new DroneDebugUpgradeHandler());
        addUpgradeRenderer(new NightVisionUpgradeHandler());
        addUpgradeRenderer(new ScubaUpgradeHandler());

        addUpgradeRenderer(new MagnetUpgradeHandler());
        addUpgradeRenderer(new ChargingUpgradeHandler());
        addUpgradeRenderer(new ChestplateLauncherHandler());
        addUpgradeRenderer(new AirConUpgradeHandler());

        addUpgradeRenderer(new RunSpeedUpgradeHandler());
        addUpgradeRenderer(new JumpBoostUpgradeHandler());

        addUpgradeRenderer(new JetBootsUpgradeHandler());
        addUpgradeRenderer(new StepAssistUpgradeHandler());
        addUpgradeRenderer(new KickUpgradeHandler());
    }

    public void refreshConfig() {
        for (EquipmentSlotType slot : ARMOR_SLOTS) {
            for (IUpgradeRenderHandler renderHandler : getHandlersForSlot(slot)) {
                renderHandler.initConfig();
            }
        }
    }

    void addUpgradeRenderer(IUpgradeRenderHandler handler) {
        upgradeRenderers.get(handler.getEquipmentSlot().getIndex()).add(handler);
        classMap.put(handler.getClass(), handler);
    }

    <T extends IUpgradeRenderHandler> T getRenderHandler(Class<T> clazz) {
        //noinspection unchecked
        return (T) classMap.get(clazz);
    }

    public List<IUpgradeRenderHandler> getHandlersForSlot(EquipmentSlotType slot) {
        return upgradeRenderers.get(slot.getIndex());
    }

    public float getAirUsage(PlayerEntity player, EquipmentSlotType slot, boolean countDisabled) {
        float totalUsage = 0;
        for (int i = 0; i < upgradeRenderers.get(slot.getIndex()).size(); i++) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
            if (handler.isUpgradeRendererInserted(slot, i) && (countDisabled || handler.isUpgradeRendererEnabled(slot, i)))
                totalUsage += upgradeRenderers.get(slot.getIndex()).get(i).getEnergyUsage(handler.getUpgradeCount(slot, EnumUpgrade.RANGE), player);
        }
        return totalUsage;
    }
}
