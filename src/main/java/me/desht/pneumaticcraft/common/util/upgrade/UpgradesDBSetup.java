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
    private static int MAX_VOLUME = 25;

    private static final Object[] DRONE_UPGRADES = new Object[] {
            EnumUpgrade.VOLUME, MAX_VOLUME,
            EnumUpgrade.INVENTORY, 35,
            EnumUpgrade.ITEM_LIFE, 10,
            EnumUpgrade.SECURITY, 3,
            EnumUpgrade.SPEED, 10,
            EnumUpgrade.ENTITY_TRACKER, 1,
            EnumUpgrade.MAGNET, 6,
            EnumUpgrade.RANGE, 16
    };
    private static final Object[] BASIC_DRONE_UPGRADES = new Object[] {
            EnumUpgrade.VOLUME, MAX_VOLUME,
            EnumUpgrade.ITEM_LIFE, 10,
            EnumUpgrade.SECURITY, 3,
            EnumUpgrade.SPEED, 10,
            EnumUpgrade.MAGNET, 6,
            EnumUpgrade.STANDBY, 1
    };

    public static void init() {
        initTileEntities();
        initEntities();
        initItems();
    }

    private static void initItems() {
        ApplicableUpgradesDB db = ApplicableUpgradesDB.getInstance();

        db.addApplicableUpgrades(ModItems.DRONE.get(), DRONE_UPGRADES);
        db.addApplicableUpgrades(ModItems.LOGISTICS_DRONE.get(), BASIC_DRONE_UPGRADES);
        db.addApplicableUpgrades(ModItems.HARVESTING_DRONE.get(), BASIC_DRONE_UPGRADES);

        db.addApplicableUpgrades(ModItems.MINIGUN.get(),
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
            addUpgrade(armor.get(slot.getIndex()), EnumUpgrade.VOLUME, MAX_VOLUME);
            addUpgrade(armor.get(slot.getIndex()), EnumUpgrade.ITEM_LIFE, PneumaticValues.ARMOR_REPAIR_MAX_UPGRADES);
            addUpgrade(armor.get(slot.getIndex()), EnumUpgrade.ARMOR, 4);
            addUpgrade(armor.get(slot.getIndex()), EnumUpgrade.THAUMCRAFT, 1);
        }
        // piece-specific upgrades which don't have a specific upgrade handler
        addUpgrade(armor.get(EquipmentSlotType.HEAD.getIndex()), EnumUpgrade.RANGE, 5);
        addUpgrade(armor.get(EquipmentSlotType.HEAD.getIndex()), EnumUpgrade.SECURITY, 64);
        addUpgrade(armor.get(EquipmentSlotType.CHEST.getIndex()), EnumUpgrade.SECURITY, 1);
        addUpgrade(armor.get(EquipmentSlotType.FEET.getIndex()), EnumUpgrade.FLIPPERS, 1);

        db.addApplicableUpgrades(ModItems.PNEUMATIC_HELMET.get(), armor.get(EquipmentSlotType.HEAD.getIndex()).toArray(new Object[0]));
        db.addApplicableUpgrades(ModItems.PNEUMATIC_CHESTPLATE.get(), armor.get(EquipmentSlotType.CHEST.getIndex()).toArray(new Object[0]));
        db.addApplicableUpgrades(ModItems.PNEUMATIC_LEGGINGS.get(), armor.get(EquipmentSlotType.LEGS.getIndex()).toArray(new Object[0]));
        db.addApplicableUpgrades(ModItems.PNEUMATIC_BOOTS.get(), armor.get(EquipmentSlotType.FEET.getIndex()).toArray(new Object[0]));
    }

    private static void initEntities() {
        ApplicableUpgradesDB db = ApplicableUpgradesDB.getInstance();

        db.addApplicableUpgrades(ModEntities.DRONE.get(), DRONE_UPGRADES);
        db.addApplicableUpgrades(ModEntities.LOGISTICS_DRONE.get(), BASIC_DRONE_UPGRADES);
        db.addApplicableUpgrades(ModEntities.HARVESTING_DRONE.get(), BASIC_DRONE_UPGRADES);
    }

    private static void initTileEntities() {
        ApplicableUpgradesDB db = ApplicableUpgradesDB.getInstance();

        db.addApplicableUpgrades(ModTileEntities.AIR_CANNON.get(),
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, MAX_VOLUME,
                EnumUpgrade.RANGE, 8,
                EnumUpgrade.ITEM_LIFE, 8,
                EnumUpgrade.ENTITY_TRACKER, 1,
                EnumUpgrade.BLOCK_TRACKER, 1,
                EnumUpgrade.DISPENSER, 1,
                EnumUpgrade.SPEED, 10);
        db.addApplicableUpgrades(ModTileEntities.AIR_COMPRESSOR.get(),
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, MAX_VOLUME,
                EnumUpgrade.SPEED, 10);
        db.addApplicableUpgrades(ModTileEntities.ADVANCED_AIR_COMPRESSOR.get(),
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, MAX_VOLUME,
                EnumUpgrade.SPEED, 10);
        db.addApplicableUpgrades(ModTileEntities.ASSEMBLY_CONTROLLER.get(),
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, MAX_VOLUME,
                EnumUpgrade.SPEED, 10);
        db.addApplicableUpgrades(ModTileEntities.CHARGING_STATION.get(),
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, MAX_VOLUME,
                EnumUpgrade.SPEED, 10,
                EnumUpgrade.DISPENSER, 1);
        db.addApplicableUpgrades(ModTileEntities.ELEVATOR_BASE.get(),
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, MAX_VOLUME,
                EnumUpgrade.SPEED, 10,
                EnumUpgrade.CHARGING, 4);
        db.addApplicableUpgrades(ModTileEntities.PNEUMATIC_DOOR_BASE.get(),
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, MAX_VOLUME,
                EnumUpgrade.SPEED, 10,
                EnumUpgrade.RANGE, 8);
        db.addApplicableUpgrades(ModTileEntities.PRESSURE_CHAMBER_INTERFACE.get(),
                EnumUpgrade.SPEED, 10,
                EnumUpgrade.DISPENSER, 1);
        db.addApplicableUpgrades(ModTileEntities.PRESSURE_CHAMBER_VALVE.get(),
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, MAX_VOLUME);
        db.addApplicableUpgrades(ModTileEntities.VACUUM_PUMP.get(),
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, MAX_VOLUME,
                EnumUpgrade.SPEED, 10);
        db.addApplicableUpgrades(ModTileEntities.UV_LIGHT_BOX.get(),
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, MAX_VOLUME,
                EnumUpgrade.DISPENSER, 1,
                EnumUpgrade.SPEED, 10);
        db.addApplicableUpgrades(ModTileEntities.SECURITY_STATION.get(),
                EnumUpgrade.ENTITY_TRACKER, 4,
                EnumUpgrade.SECURITY, 64,
                EnumUpgrade.RANGE, 14);
        db.addApplicableUpgrades(ModTileEntities.AERIAL_INTERFACE.get(),
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, MAX_VOLUME,
                EnumUpgrade.DISPENSER, 1);
        db.addApplicableUpgrades(ModTileEntities.ELECTROSTATIC_COMPRESSOR.get(),
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, MAX_VOLUME);
        db.addApplicableUpgrades(ModTileEntities.OMNIDIRECTIONAL_HOPPER.get(),
                EnumUpgrade.SPEED, 11,
                EnumUpgrade.CREATIVE, 1,
                EnumUpgrade.DISPENSER, 1);
        db.addApplicableUpgrades(ModTileEntities.LIQUID_HOPPER.get(),
                EnumUpgrade.SPEED, 11,
                EnumUpgrade.CREATIVE, 1,
                EnumUpgrade.DISPENSER, 1);
        db.addApplicableUpgrades(ModTileEntities.LIQUID_COMPRESSOR.get(),
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, MAX_VOLUME,
                EnumUpgrade.SPEED, 10);
        db.addApplicableUpgrades(ModTileEntities.ADVANCED_LIQUID_COMPRESSOR.get(),
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, MAX_VOLUME,
                EnumUpgrade.SPEED, 10);
        db.addApplicableUpgrades(ModTileEntities.PROGRAMMABLE_CONTROLLER.get(),
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, MAX_VOLUME,
                EnumUpgrade.SPEED, 10,
                EnumUpgrade.INVENTORY, 35);
        db.addApplicableUpgrades(ModTileEntities.GAS_LIFT.get(),
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, MAX_VOLUME,
                EnumUpgrade.SPEED, 10,
                EnumUpgrade.DISPENSER, 1);
        db.addApplicableUpgrades(ModTileEntities.THERMOPNEUMATIC_PROCESSING_PLANT.get(),
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, MAX_VOLUME,
                EnumUpgrade.DISPENSER, 1);
        db.addApplicableUpgrades(ModTileEntities.SENTRY_TURRET.get(),
                EnumUpgrade.RANGE, 16);
        db.addApplicableUpgrades(ModTileEntities.FLUX_COMPRESSOR.get(),
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, MAX_VOLUME,
                EnumUpgrade.SPEED, 10);
        db.addApplicableUpgrades(ModTileEntities.PNEUMATIC_DYNAMO.get(),
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, MAX_VOLUME,
                EnumUpgrade.SPEED, 10);
        db.addApplicableUpgrades(ModTileEntities.THERMAL_COMPRESSOR.get(),
                EnumUpgrade.SECURITY, 1,
                EnumUpgrade.VOLUME, MAX_VOLUME);
        db.addApplicableUpgrades(ModTileEntities.TANK_SMALL.get(),
                EnumUpgrade.SPEED, 7,
                EnumUpgrade.DISPENSER, 1);
        db.addApplicableUpgrades(ModTileEntities.TANK_MEDIUM.get(),
                EnumUpgrade.SPEED, 7,
                EnumUpgrade.DISPENSER, 1);
        db.addApplicableUpgrades(ModTileEntities.TANK_LARGE.get(),
                EnumUpgrade.SPEED, 7,
                EnumUpgrade.DISPENSER, 1);

        // universal sensor needs some dynamic calculation...
        List<Object> l = new ArrayList<>();
        SensorHandler.getInstance().getUniversalSensorUpgrades().forEach(upgrade -> addUpgrade(l, upgrade, 1));
        addUpgrade(l, EnumUpgrade.RANGE, 64);
        addUpgrade(l, EnumUpgrade.SECURITY, 1);
        addUpgrade(l, EnumUpgrade.VOLUME, MAX_VOLUME);
        db.addApplicableUpgrades(ModTileEntities.UNIVERSAL_SENSOR.get(), l.toArray(new Object[0]));
    }

    private static void addUpgrade(List<Object> l, EnumUpgrade upgrade, int n) {
        if (upgrade.isDepLoaded()) {
            l.add(upgrade);
            l.add(n);
        }
    }

}
