package me.desht.pneumaticcraft.common.util.upgrade;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.common.core.ModEntities;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.sensor.SensorHandler;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.inventory.EquipmentSlotType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpgradesDBSetup {
    private static final Object[] DRONE_UPGRADES = new Object[] {
            EnumUpgrade.VOLUME, 10,
            EnumUpgrade.INVENTORY, 35,
            EnumUpgrade.ITEM_LIFE, 10,
            EnumUpgrade.SECURITY, 3,
            EnumUpgrade.SPEED, 10,
            EnumUpgrade.ENTITY_TRACKER, 1,
            EnumUpgrade.MAGNET, 6,
            EnumUpgrade.RANGE, 16
    };
    private static final Object[] BASIC_DRONE_UPGRADES = new Object[] {
            EnumUpgrade.VOLUME, 10,
            EnumUpgrade.ITEM_LIFE, 10,
            EnumUpgrade.SECURITY, 3,
            EnumUpgrade.SPEED, 10,
            EnumUpgrade.MAGNET, 6
    };

    public static void init() {
        initTileEntities();
        initEntities();
        initItems();
    }

    private static void initItems() {
        ApplicableUpgradesDB db = ApplicableUpgradesDB.getInstance();

        db.addApplicableUpgrades(ModItems.DRONE, DRONE_UPGRADES);
        db.addApplicableUpgrades(ModItems.LOGISTIC_DRONE, BASIC_DRONE_UPGRADES);
        db.addApplicableUpgrades(ModItems.HARVESTING_DRONE, BASIC_DRONE_UPGRADES);

        db.addApplicableUpgrades(ModItems.MINIGUN,
                EnumUpgrade.SPEED, 3,
                EnumUpgrade.RANGE, 6,
                EnumUpgrade.DISPENSER, 3,
                EnumUpgrade.ITEM_LIFE, 4,
                EnumUpgrade.ENTITY_TRACKER, 4,
                EnumUpgrade.SECURITY, 1);

        // Pneumatic Armor
        List<List<Object>> armor = new ArrayList<>();
        for (EquipmentSlotType ignored : UpgradeRenderHandlerList.ARMOR_SLOTS) {
            armor.add(new ArrayList<>());
        }
        for (EquipmentSlotType slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
            // upgrades automatically added due to an upgrade handler being registered
            UpgradeRenderHandlerList.instance().getHandlersForSlot(slot).forEach(
                    handler -> Arrays.stream(handler.getRequiredUpgrades())
                            .forEach(upgrade -> {
                                addUpgrade(armor.get(slot.getIndex()), upgrade, handler.getMaxInstallableUpgrades(upgrade));
                            })
            );
            // upgrades common to all armor pieces without a specific upgrade handler
            addUpgrade(armor.get(slot.getIndex()), EnumUpgrade.SPEED, 10);
            addUpgrade(armor.get(slot.getIndex()), EnumUpgrade.VOLUME, 10);
            addUpgrade(armor.get(slot.getIndex()), EnumUpgrade.ITEM_LIFE, PneumaticValues.ARMOR_REPAIR_MAX_UPGRADES);
            addUpgrade(armor.get(slot.getIndex()), EnumUpgrade.ARMOR, 4);
            addUpgrade(armor.get(slot.getIndex()), EnumUpgrade.THAUMCRAFT, 1);
        }
        // piece-specific upgrades which don't have a specific upgrade handler
        addUpgrade(armor.get(EquipmentSlotType.HEAD.getIndex()), EnumUpgrade.RANGE, 5);
        addUpgrade(armor.get(EquipmentSlotType.HEAD.getIndex()), EnumUpgrade.SECURITY, 64);
        addUpgrade(armor.get(EquipmentSlotType.CHEST.getIndex()), EnumUpgrade.SECURITY, 1);
        addUpgrade(armor.get(EquipmentSlotType.FEET.getIndex()), EnumUpgrade.FLIPPERS, 1);

        db.addApplicableUpgrades(ModItems.PNEUMATIC_HELMET, armor.get(EquipmentSlotType.HEAD.getIndex()).toArray(new Object[0]));
        db.addApplicableUpgrades(ModItems.PNEUMATIC_CHESTPLATE, armor.get(EquipmentSlotType.CHEST.getIndex()).toArray(new Object[0]));
        db.addApplicableUpgrades(ModItems.PNEUMATIC_LEGGINGS, armor.get(EquipmentSlotType.LEGS.getIndex()).toArray(new Object[0]));
        db.addApplicableUpgrades(ModItems.PNEUMATIC_BOOTS, armor.get(EquipmentSlotType.FEET.getIndex()).toArray(new Object[0]));
    }

    private static void initEntities() {
        ApplicableUpgradesDB db = ApplicableUpgradesDB.getInstance();

        db.addApplicableUpgrades(ModEntities.DRONE, DRONE_UPGRADES);
        db.addApplicableUpgrades(ModEntities.LOGISTIC_DRONE, BASIC_DRONE_UPGRADES);
        db.addApplicableUpgrades(ModEntities.HARVESTING_DRONE, BASIC_DRONE_UPGRADES);
    }

    private static void initTileEntities() {
        ApplicableUpgradesDB db = ApplicableUpgradesDB.getInstance();

        db.addApplicableUpgrades(ModTileEntities.AIR_CANNON,
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, 10,
                EnumUpgrade.RANGE, 8,
                EnumUpgrade.ITEM_LIFE, 8,
                EnumUpgrade.ENTITY_TRACKER, 1,
                EnumUpgrade.BLOCK_TRACKER, 1,
                EnumUpgrade.DISPENSER, 1,
                EnumUpgrade.SPEED, 10);
        db.addApplicableUpgrades(ModTileEntities.AIR_COMPRESSOR,
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, 10,
                EnumUpgrade.SPEED, 10);
        db.addApplicableUpgrades(ModTileEntities.ADVANCED_AIR_COMPRESSOR,
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, 10,
                EnumUpgrade.SPEED, 10);
        db.addApplicableUpgrades(ModTileEntities.ASSEMBLY_CONTROLLER,
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, 10,
                EnumUpgrade.SPEED, 10);
        db.addApplicableUpgrades(ModTileEntities.CHARGING_STATION,
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, 10,
                EnumUpgrade.SPEED, 10,
                EnumUpgrade.DISPENSER, 1);
        db.addApplicableUpgrades(ModTileEntities.ELEVATOR_BASE,
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, 10,
                EnumUpgrade.SPEED, 10,
                EnumUpgrade.CHARGING, 4);
        db.addApplicableUpgrades(ModTileEntities.PNEUMATIC_DOOR_BASE,
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, 10,
                EnumUpgrade.SPEED, 10,
                EnumUpgrade.RANGE, 8);
        db.addApplicableUpgrades(ModTileEntities.PRESSURE_CHAMBER_INTERFACE,
                EnumUpgrade.SPEED, 10,
                EnumUpgrade.DISPENSER, 1);
        db.addApplicableUpgrades(ModTileEntities.PRESSURE_CHAMBER_VALVE,
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, 10);
        db.addApplicableUpgrades(ModTileEntities.VACUUM_PUMP,
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, 10,
                EnumUpgrade.SPEED, 10);
        db.addApplicableUpgrades(ModTileEntities.UV_LIGHT_BOX,
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, 10,
                EnumUpgrade.SPEED, 10);
        db.addApplicableUpgrades(ModTileEntities.SECURITY_STATION,
                EnumUpgrade.ENTITY_TRACKER, 4,
                EnumUpgrade.SECURITY, 64,
                EnumUpgrade.RANGE, 14);
        db.addApplicableUpgrades(ModTileEntities.AERIAL_INTERFACE,
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, 10,
                EnumUpgrade.DISPENSER, 1);
        db.addApplicableUpgrades(ModTileEntities.ELECTROSTATIC_COMPRESSOR,
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, 10);
        db.addApplicableUpgrades(ModTileEntities.OMNIDIRECTIONAL_HOPPER,
                EnumUpgrade.SPEED, 11,
                EnumUpgrade.CREATIVE, 1,
                EnumUpgrade.DISPENSER, 1);
        db.addApplicableUpgrades(ModTileEntities.LIQUID_HOPPER,
                EnumUpgrade.SPEED, 11,
                EnumUpgrade.CREATIVE, 1,
                EnumUpgrade.DISPENSER, 1);
        db.addApplicableUpgrades(ModTileEntities.LIQUID_COMPRESSOR,
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, 10,
                EnumUpgrade.SPEED, 10);
        db.addApplicableUpgrades(ModTileEntities.ADVANCED_LIQUID_COMPRESSOR,
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, 10,
                EnumUpgrade.SPEED, 10);
        db.addApplicableUpgrades(ModTileEntities.PROGRAMMABLE_CONTROLLER,
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, 10,
                EnumUpgrade.SPEED, 10,
                EnumUpgrade.INVENTORY, 35);
        db.addApplicableUpgrades(ModTileEntities.GAS_LIFT,
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, 10,
                EnumUpgrade.SPEED, 10,
                EnumUpgrade.DISPENSER, 1);
        db.addApplicableUpgrades(ModTileEntities.THERMOPNEUMATIC_PROCESSING_PLANT,
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, 10,
                EnumUpgrade.DISPENSER, 1);
        db.addApplicableUpgrades(ModTileEntities.SENTRY_TURRET,
                EnumUpgrade.RANGE, 16);
        db.addApplicableUpgrades(ModTileEntities.FLUX_COMPRESSOR,
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, 10,
                EnumUpgrade.SPEED, 10);
        db.addApplicableUpgrades(ModTileEntities.PNEUMATIC_DYNAMO,
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, 10,
                EnumUpgrade.SPEED, 10);
        db.addApplicableUpgrades(ModTileEntities.THERMAL_COMPRESSOR,
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, 10);

        // universal sensor needs some dynamic calculation...
        List<Object> l = new ArrayList<>();
        SensorHandler.getInstance().getUniversalSensorUpgrades().forEach(upgrade -> addUpgrade(l, upgrade, 1));
        addUpgrade(l, EnumUpgrade.RANGE, 64);
        addUpgrade(l, EnumUpgrade.SECURITY, 1);
        addUpgrade(l, EnumUpgrade.VOLUME, 10);
        db.addApplicableUpgrades(ModTileEntities.UNIVERSAL_SENSOR, l.toArray(new Object[0]));
    }

    private static void addUpgrade(List<Object> l, EnumUpgrade upgrade, int n) {
        if (upgrade.isDepLoaded()) {
            l.add(upgrade);
            l.add(n);
        }
    }

}
