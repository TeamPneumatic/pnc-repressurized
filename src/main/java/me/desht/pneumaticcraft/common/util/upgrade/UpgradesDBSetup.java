package me.desht.pneumaticcraft.common.util.upgrade;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.core.ModEntities;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.sensor.SensorHandler;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.inventory.EquipmentSlotType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UpgradesDBSetup {
    private static final int MAX_VOLUME = 25;

    private static final Builder DRONE_UPGRADES = new Builder()
            .with(EnumUpgrade.VOLUME, MAX_VOLUME)
            .with(EnumUpgrade.INVENTORY, 35)
            .with(EnumUpgrade.ITEM_LIFE, 10)
            .with(EnumUpgrade.SECURITY, 3)
            .with(EnumUpgrade.SPEED, 10)
            .with(EnumUpgrade.MINIGUN, 1)
            .with(EnumUpgrade.MAGNET, 6)
            .with(EnumUpgrade.ARMOR, 15)
            .with(EnumUpgrade.RANGE, 16);
    
    private static final Builder BASIC_DRONE_UPGRADES = new Builder()
            .with(EnumUpgrade.VOLUME, MAX_VOLUME)
            .with(EnumUpgrade.ITEM_LIFE, 10)
            .with(EnumUpgrade.SECURITY, 3)
            .with(EnumUpgrade.SPEED, 10)
            .with(EnumUpgrade.STANDBY, 1);

    private static final Builder GUARD_DRONE_UPGRADES = new Builder(BASIC_DRONE_UPGRADES)
            .with(EnumUpgrade.MINIGUN, 1)
            .with(EnumUpgrade.ARMOR, 15)
            .with(EnumUpgrade.RANGE, 16);

    private static final Builder COLLECTOR_DRONE_UPGRADES = new Builder(BASIC_DRONE_UPGRADES)
            .with(EnumUpgrade.MAGNET, 6)
            .with(EnumUpgrade.RANGE, 16)
            .with(EnumUpgrade.INVENTORY, 35);

    private static final Builder LOGISTICS_DRONE_UPGRADES = new Builder(BASIC_DRONE_UPGRADES)
            .with(EnumUpgrade.INVENTORY, 35);

    public static void init() {
        initTileEntities();
        initEntities();
        initItems();
    }

    private static void initItems() {
        ApplicableUpgradesDB db = ApplicableUpgradesDB.getInstance();

        db.addApplicableUpgrades(ModItems.DRONE.get(), DRONE_UPGRADES);
        db.addApplicableUpgrades(ModItems.HARVESTING_DRONE.get(), BASIC_DRONE_UPGRADES);
        db.addApplicableUpgrades(ModItems.GUARD_DRONE.get(), GUARD_DRONE_UPGRADES);
        db.addApplicableUpgrades(ModItems.COLLECTOR_DRONE.get(), COLLECTOR_DRONE_UPGRADES);
        db.addApplicableUpgrades(ModItems.LOGISTICS_DRONE.get(), LOGISTICS_DRONE_UPGRADES);

        db.addApplicableUpgrades(ModItems.MINIGUN.get(), new Builder()
                .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                .with(EnumUpgrade.SPEED, 3)
                .with(EnumUpgrade.RANGE, 6)
                .with(EnumUpgrade.DISPENSER, 3)
                .with(EnumUpgrade.ITEM_LIFE, 4)
                .with(EnumUpgrade.ENTITY_TRACKER, 4)
                .with(EnumUpgrade.SECURITY, 1));

        db.addApplicableUpgrades(ModItems.JACKHAMMER.get(), new Builder()
                .with(EnumUpgrade.SPEED, 10)
                .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                .with(EnumUpgrade.MAGNET, 1)
        );

        // Pneumatic Armor
        List<Builder> armor = Arrays.asList(new Builder(), new Builder(), new Builder(), new Builder());
        for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            // upgrades automatically added due to an upgrade handler being registered
            ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot).forEach(handler ->
                    Arrays.stream(handler.getRequiredUpgrades()).forEach(upgrade -> {
                        armor.get(slot.getIndex()).with(upgrade, handler.getMaxInstallableUpgrades(upgrade));
                    })
            );
            // upgrades common to all armor pieces without a specific upgrade handler
            armor.get(slot.getIndex()).with(EnumUpgrade.SPEED, 10)
                    .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                    .with(EnumUpgrade.ITEM_LIFE, PneumaticValues.ARMOR_REPAIR_MAX_UPGRADES)
                    .with(EnumUpgrade.ARMOR, 4)
                    .with(EnumUpgrade.THAUMCRAFT, 1);
        }
        // piece-specific upgrades which don't have a specific upgrade handler
        armor.get(EquipmentSlotType.HEAD.getIndex()).with(EnumUpgrade.RANGE, 5).with(EnumUpgrade.SECURITY, 64);
        armor.get(EquipmentSlotType.CHEST.getIndex()).with(EnumUpgrade.SECURITY, 1);
        armor.get(EquipmentSlotType.FEET.getIndex()).with(EnumUpgrade.FLIPPERS, 1);

        db.addApplicableUpgrades(ModItems.PNEUMATIC_HELMET.get(), armor.get(EquipmentSlotType.HEAD.getIndex()));
        db.addApplicableUpgrades(ModItems.PNEUMATIC_CHESTPLATE.get(), armor.get(EquipmentSlotType.CHEST.getIndex()));
        db.addApplicableUpgrades(ModItems.PNEUMATIC_LEGGINGS.get(), armor.get(EquipmentSlotType.LEGS.getIndex()));
        db.addApplicableUpgrades(ModItems.PNEUMATIC_BOOTS.get(), armor.get(EquipmentSlotType.FEET.getIndex()));
    }

    private static void initEntities() {
        ApplicableUpgradesDB db = ApplicableUpgradesDB.getInstance();

        db.addApplicableUpgrades(ModEntities.DRONE.get(), DRONE_UPGRADES);
        db.addApplicableUpgrades(ModEntities.HARVESTING_DRONE.get(), BASIC_DRONE_UPGRADES);
        db.addApplicableUpgrades(ModEntities.GUARD_DRONE.get(), GUARD_DRONE_UPGRADES);
        db.addApplicableUpgrades(ModEntities.COLLECTOR_DRONE.get(), COLLECTOR_DRONE_UPGRADES);
        db.addApplicableUpgrades(ModEntities.LOGISTICS_DRONE.get(), LOGISTICS_DRONE_UPGRADES);
    }

    private static void initTileEntities() {
        ApplicableUpgradesDB db = ApplicableUpgradesDB.getInstance();

        db.addApplicableUpgrades(ModTileEntities.AIR_CANNON.get(), new Builder()
                .with(EnumUpgrade.SECURITY, 1)
                .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                .with(EnumUpgrade.RANGE, 8)
                .with(EnumUpgrade.ITEM_LIFE, 8)
                .with(EnumUpgrade.ENTITY_TRACKER, 1)
                .with(EnumUpgrade.BLOCK_TRACKER, 1)
                .with(EnumUpgrade.DISPENSER, 1)
                .with(EnumUpgrade.SPEED, 10));
        db.addApplicableUpgrades(ModTileEntities.AIR_COMPRESSOR.get(), new Builder()
                .with(EnumUpgrade.SECURITY, 1)
                .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                .with(EnumUpgrade.SPEED, 10));
        db.addApplicableUpgrades(ModTileEntities.ADVANCED_AIR_COMPRESSOR.get(), new Builder()
                .with(EnumUpgrade.SECURITY, 1)
                .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                .with(EnumUpgrade.SPEED, 10));
        db.addApplicableUpgrades(ModTileEntities.ASSEMBLY_CONTROLLER.get(), new Builder()
                .with(EnumUpgrade.SECURITY, 1)
                .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                .with(EnumUpgrade.SPEED, 10));
        db.addApplicableUpgrades(ModTileEntities.CHARGING_STATION.get(), new Builder()
                .with(EnumUpgrade.SECURITY, 1)
                .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                .with(EnumUpgrade.SPEED, 10)
                .with(EnumUpgrade.DISPENSER, 1));
        db.addApplicableUpgrades(ModTileEntities.ELEVATOR_BASE.get(), new Builder()
                .with(EnumUpgrade.SECURITY, 1)
                .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                .with(EnumUpgrade.SPEED, 10)
                .with(EnumUpgrade.CHARGING, 4));
        db.addApplicableUpgrades(ModTileEntities.PNEUMATIC_DOOR_BASE.get(), new Builder()
                .with(EnumUpgrade.SECURITY, 1)
                .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                .with(EnumUpgrade.SPEED, 10)
                .with(EnumUpgrade.RANGE, 8));
        db.addApplicableUpgrades(ModTileEntities.PRESSURE_CHAMBER_INTERFACE.get(), new Builder()
                .with(EnumUpgrade.SPEED, 10)
                .with(EnumUpgrade.DISPENSER, 1));
        db.addApplicableUpgrades(ModTileEntities.PRESSURE_CHAMBER_VALVE.get(), new Builder()
                .with(EnumUpgrade.SECURITY, 1)
                .with(EnumUpgrade.VOLUME, MAX_VOLUME));
        db.addApplicableUpgrades(ModTileEntities.VACUUM_PUMP.get(), new Builder()
                .with(EnumUpgrade.SECURITY, 1)
                .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                .with(EnumUpgrade.SPEED, 10));
        db.addApplicableUpgrades(ModTileEntities.UV_LIGHT_BOX.get(), new Builder()
                .with(EnumUpgrade.SECURITY, 1)
                .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                .with(EnumUpgrade.DISPENSER, 1)
                .with(EnumUpgrade.SPEED, 10));
        db.addApplicableUpgrades(ModTileEntities.SECURITY_STATION.get(), new Builder()
                .with(EnumUpgrade.ENTITY_TRACKER, 12)
                .with(EnumUpgrade.SECURITY, 64)
                .with(EnumUpgrade.RANGE, 14));
        db.addApplicableUpgrades(ModTileEntities.AERIAL_INTERFACE.get(), new Builder()
                .with(EnumUpgrade.SECURITY, 1)
                .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                .with(EnumUpgrade.DISPENSER, 1));
        db.addApplicableUpgrades(ModTileEntities.ELECTROSTATIC_COMPRESSOR.get(), new Builder()
                .with(EnumUpgrade.SECURITY, 1)
                .with(EnumUpgrade.VOLUME, MAX_VOLUME));
        db.addApplicableUpgrades(ModTileEntities.OMNIDIRECTIONAL_HOPPER.get(), new Builder()
                .with(EnumUpgrade.SPEED, 11)
                .with(EnumUpgrade.CREATIVE, 1)
                .with(EnumUpgrade.ENTITY_TRACKER, 1)
                .with(EnumUpgrade.DISPENSER, 1));
        db.addApplicableUpgrades(ModTileEntities.LIQUID_HOPPER.get(), new Builder()
                .with(EnumUpgrade.SPEED, 11)
                .with(EnumUpgrade.CREATIVE, 1)
                .with(EnumUpgrade.ENTITY_TRACKER, 1)
                .with(EnumUpgrade.DISPENSER, 1));
        db.addApplicableUpgrades(ModTileEntities.LIQUID_COMPRESSOR.get(), new Builder()
                .with(EnumUpgrade.SECURITY, 1)
                .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                .with(EnumUpgrade.SPEED, 10));
        db.addApplicableUpgrades(ModTileEntities.ADVANCED_LIQUID_COMPRESSOR.get(), new Builder()
                .with(EnumUpgrade.SECURITY, 1)
                .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                .with(EnumUpgrade.SPEED, 10));
        db.addApplicableUpgrades(ModTileEntities.PROGRAMMABLE_CONTROLLER.get(), new Builder()
                .with(EnumUpgrade.SECURITY, 1)
                .with(EnumUpgrade.MAGNET, 6)
                .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                .with(EnumUpgrade.SPEED, 10)
                .with(EnumUpgrade.INVENTORY, 35));
        db.addApplicableUpgrades(ModTileEntities.GAS_LIFT.get(), new Builder()
                .with(EnumUpgrade.SECURITY, 1)
                .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                .with(EnumUpgrade.SPEED, 10)
                .with(EnumUpgrade.DISPENSER, 1));
        db.addApplicableUpgrades(ModTileEntities.THERMOPNEUMATIC_PROCESSING_PLANT.get(), new Builder()
                .with(EnumUpgrade.SECURITY, 1)
                .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                .with(EnumUpgrade.DISPENSER, 1));
        db.addApplicableUpgrades(ModTileEntities.SENTRY_TURRET.get(), new Builder()
                .with(EnumUpgrade.RANGE, 16));
        db.addApplicableUpgrades(ModTileEntities.FLUX_COMPRESSOR.get(), new Builder()
                .with(EnumUpgrade.SECURITY, 1)
                .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                .with(EnumUpgrade.SPEED, 10));
        db.addApplicableUpgrades(ModTileEntities.PNEUMATIC_DYNAMO.get(), new Builder()
                .with(EnumUpgrade.SECURITY, 1)
                .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                .with(EnumUpgrade.SPEED, 10));
        db.addApplicableUpgrades(ModTileEntities.THERMAL_COMPRESSOR.get(), new Builder()
                .with(EnumUpgrade.SECURITY, 1)
                .with(EnumUpgrade.VOLUME, MAX_VOLUME));
        db.addApplicableUpgrades(ModTileEntities.TANK_SMALL.get(), new Builder()
                .with(EnumUpgrade.SPEED, 7)
                .with(EnumUpgrade.DISPENSER, 1));
        db.addApplicableUpgrades(ModTileEntities.TANK_MEDIUM.get(), new Builder()
                .with(EnumUpgrade.SPEED, 7)
                .with(EnumUpgrade.DISPENSER, 1));
        db.addApplicableUpgrades(ModTileEntities.TANK_LARGE.get(), new Builder()
                .with(EnumUpgrade.SPEED, 7)
                .with(EnumUpgrade.DISPENSER, 1));
        db.addApplicableUpgrades(ModTileEntities.SMART_CHEST.get(), new Builder()
                .with(EnumUpgrade.SPEED, 9)
                .with(EnumUpgrade.DISPENSER, 1)
                .with(EnumUpgrade.MAGNET, 1)
                .with(EnumUpgrade.RANGE, 4));
        db.addApplicableUpgrades(ModTileEntities.FLUID_MIXER.get(), new Builder()
                .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                .with(EnumUpgrade.SECURITY, 1)
                .with(EnumUpgrade.DISPENSER, 1));
        db.addApplicableUpgrades(ModTileEntities.VACUUM_TRAP.get(), new Builder()
                .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                .with(EnumUpgrade.RANGE, 6)
                .with(EnumUpgrade.SECURITY, 1));
        db.addApplicableUpgrades(ModTileEntities.SPAWNER_EXTRACTOR.get(), new Builder()
                .with(EnumUpgrade.VOLUME, MAX_VOLUME));
        db.addApplicableUpgrades(ModTileEntities.PRESSURIZED_SPAWNER.get(), new Builder()
                .with(EnumUpgrade.VOLUME, MAX_VOLUME)
                .with(EnumUpgrade.RANGE, 6)
                .with(EnumUpgrade.SPEED, 10)
                .with(EnumUpgrade.SECURITY, 1));

        // universal sensor needs some dynamic calculation...
        Builder sensorBuilder = new Builder();
        SensorHandler.getInstance().getUniversalSensorUpgrades().forEach(upgrade -> sensorBuilder.with(upgrade, 1));
        sensorBuilder.with(EnumUpgrade.RANGE, 64).with(EnumUpgrade.SECURITY, 1).with(EnumUpgrade.VOLUME, MAX_VOLUME);
        db.addApplicableUpgrades(ModTileEntities.UNIVERSAL_SENSOR.get(), sensorBuilder);
    }

    static class Builder {
        private final List<Integer> l;

        Builder() {
            l = new ArrayList<>(Collections.nCopies(EnumUpgrade.values().length, 0));
        }

        Builder(Builder copy) {
            l = new ArrayList<>(copy.upgrades());
        }

        Builder with(EnumUpgrade upgrade, int amount) {
            l.set(upgrade.ordinal(), amount);
            return this;
        }

        List<Integer> upgrades() {
            return l;
        }

        public int[] build() {
            return l.stream().mapToInt(Integer::intValue).toArray();
        }
    }
}
