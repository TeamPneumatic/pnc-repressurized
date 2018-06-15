package me.desht.pneumaticcraft.client.render.pneumaticArmor;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpgradeRenderHandlerList {
    private static UpgradeRenderHandlerList INSTANCE;

    private final List<List<IUpgradeRenderHandler>> upgradeRenderers;

    public static UpgradeRenderHandlerList instance() {
        return INSTANCE;
    }

    public static void init() {
        INSTANCE = new UpgradeRenderHandlerList();
    }

    private final Map<Class<? extends IUpgradeRenderHandler>, IUpgradeRenderHandler> classMap = new HashMap<>();
    private final Map<String, IUpgradeRenderHandler> nameMap = new HashMap<>();

    // convenience
    public static final EntityEquipmentSlot[] ARMOR_SLOTS = new EntityEquipmentSlot[4];
    static {
        ARMOR_SLOTS[0] = EntityEquipmentSlot.HEAD;
        ARMOR_SLOTS[1] = EntityEquipmentSlot.CHEST;
        ARMOR_SLOTS[2] = EntityEquipmentSlot.LEGS;
        ARMOR_SLOTS[3] = EntityEquipmentSlot.FEET;
    }

    private UpgradeRenderHandlerList() {
        upgradeRenderers = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            upgradeRenderers.add(new ArrayList<>());
        }
        addUpgradeRenderer(new MainHelmetHandler());
        addUpgradeRenderer(new BlockTrackUpgradeHandler());
        addUpgradeRenderer(new EntityTrackUpgradeHandler());
        addUpgradeRenderer(new SearchUpgradeHandler());
        addUpgradeRenderer(new CoordTrackUpgradeHandler());
        addUpgradeRenderer(new DroneDebugUpgradeHandler());
        addUpgradeRenderer(new MagnetUpgradeRenderHandler());
    }

    public void addUpgradeRenderer(IUpgradeRenderHandler handler) {
        upgradeRenderers.get(handler.getEquipmentSlot().getIndex()).add(handler);
        classMap.put(handler.getClass(), handler);
        nameMap.put(handler.getUpgradeName(), handler);
    }

    public <T extends IUpgradeRenderHandler> T getRenderHandler(Class<T> clazz) {
        return (T) classMap.get(clazz);
    }

    public IUpgradeRenderHandler getRenderHandlerByName(String name) {
        return nameMap.get(name);
    }

    public List<IUpgradeRenderHandler> getHandlersForSlot(EntityEquipmentSlot slot) {
        return upgradeRenderers.get(slot.getIndex());
    }

    public float getAirUsage(EntityPlayer player, EntityEquipmentSlot slot, boolean countDisabled) {
        float totalUsage = 0;
        for (int i = 0; i < upgradeRenderers.get(slot.getIndex()).size(); i++) {
            if (CommonHUDHandler.getHandlerForPlayer(player).isUpgradeRendererInserted(slot, i) && (countDisabled || CommonHUDHandler.getHandlerForPlayer(player).isUpgradeRendererEnabled(slot, i)))
                totalUsage += upgradeRenderers.get(slot.getIndex()).get(i).getEnergyUsage(CommonHUDHandler.getHandlerForPlayer(player).rangeUpgradesInstalled, player);
        }
        return totalUsage;
    }
}
