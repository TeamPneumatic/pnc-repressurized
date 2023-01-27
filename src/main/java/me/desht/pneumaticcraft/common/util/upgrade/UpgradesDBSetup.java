/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.util.upgrade;

import me.desht.pneumaticcraft.api.item.IUpgradeRegistry.Builder;
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import me.desht.pneumaticcraft.common.core.ModEntityTypes;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModUpgrades;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.sensor.SensorHandler;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.Arrays;
import java.util.List;

public class UpgradesDBSetup {
    private static final int MAX_VOLUME = 25;

    private static final Builder DRONE_UPGRADES = new Builder()
            .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
            .with(ModUpgrades.INVENTORY.get(), 35)
            .with(ModUpgrades.ITEM_LIFE.get(), 10)
            .with(ModUpgrades.SECURITY.get(), 3)
            .with(ModUpgrades.SPEED.get(), 10)
            .with(ModUpgrades.MINIGUN.get(), 1)
            .with(ModUpgrades.MAGNET.get(), 6)
            .with(ModUpgrades.ARMOR.get(), 15)
            .with(ModUpgrades.RANGE.get(), 16);
    
    private static final Builder BASIC_DRONE_UPGRADES = new Builder()
            .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
            .with(ModUpgrades.ITEM_LIFE.get(), 10)
            .with(ModUpgrades.SECURITY.get(), 3)
            .with(ModUpgrades.SPEED.get(), 10)
            .with(ModUpgrades.STANDBY.get(), 1);

    private static final Builder GUARD_DRONE_UPGRADES = Builder.copyOf(BASIC_DRONE_UPGRADES)
            .with(ModUpgrades.MINIGUN.get(), 1)
            .with(ModUpgrades.ARMOR.get(), 15)
            .with(ModUpgrades.RANGE.get(), 16);

    private static final Builder COLLECTOR_DRONE_UPGRADES = Builder.copyOf(BASIC_DRONE_UPGRADES)
            .with(ModUpgrades.MAGNET.get(), 6)
            .with(ModUpgrades.RANGE.get(), 16)
            .with(ModUpgrades.INVENTORY.get(), 35);

    private static final Builder LOGISTICS_DRONE_UPGRADES = Builder.copyOf(BASIC_DRONE_UPGRADES)
            .with(ModUpgrades.INVENTORY.get(), 35);

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
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.SPEED.get(), 3)
                .with(ModUpgrades.RANGE.get(), 6)
                .with(ModUpgrades.DISPENSER.get(), 3)
                .with(ModUpgrades.ITEM_LIFE.get(), 4)
                .with(ModUpgrades.ENTITY_TRACKER.get(), 4)
                .with(ModUpgrades.SECURITY.get(), 1));

        db.addApplicableUpgrades(ModItems.JACKHAMMER.get(), new Builder()
                .with(ModUpgrades.SPEED.get(), 10)
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.MAGNET.get(), 1)
        );

        db.addApplicableUpgrades(ModItems.AMADRON_TABLET.get(), new Builder()
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
        );

        // Pneumatic Armor
        List<Builder> armor = Arrays.asList(new Builder(), new Builder(), new Builder(), new Builder());
        for (EquipmentSlot slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            // upgrades automatically added due to an upgrade handler being registered
            ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot)
                    .forEach(handler -> Arrays.stream(handler.getRequiredUpgrades())
                            .forEach(upgrade -> armor.get(slot.getIndex()).with(upgrade, handler.getMaxInstallableUpgrades(upgrade)))
            );
            // upgrades common to all armor pieces without a specific upgrade handler
            armor.get(slot.getIndex()).with(ModUpgrades.SPEED.get(), 10)
                    .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                    .with(ModUpgrades.ITEM_LIFE.get(), PneumaticValues.ARMOR_REPAIR_MAX_UPGRADES)
                    .with(ModUpgrades.ARMOR.get(), 4)
                    .with(ModUpgrades.RADIATION_SHIELDING.get(), 1)
                    .with(ModUpgrades.GILDED.get(), 1)
                    .with(ModUpgrades.THAUMCRAFT.get(), 1)
                    .with(ModUpgrades.CREATIVE.get(), 1);
        }
        // piece-specific upgrades which don't have a specific upgrade handler
        armor.get(EquipmentSlot.HEAD.getIndex()).with(ModUpgrades.RANGE.get(), 5).with(ModUpgrades.SECURITY.get(), 64);
        armor.get(EquipmentSlot.CHEST.getIndex()).with(ModUpgrades.SECURITY.get(), 1);
        armor.get(EquipmentSlot.FEET.getIndex()).with(ModUpgrades.FLIPPERS.get(), 1);

        db.addApplicableUpgrades(ModItems.PNEUMATIC_HELMET.get(), armor.get(EquipmentSlot.HEAD.getIndex()));
        db.addApplicableUpgrades(ModItems.PNEUMATIC_CHESTPLATE.get(), armor.get(EquipmentSlot.CHEST.getIndex()));
        db.addApplicableUpgrades(ModItems.PNEUMATIC_LEGGINGS.get(), armor.get(EquipmentSlot.LEGS.getIndex()));
        db.addApplicableUpgrades(ModItems.PNEUMATIC_BOOTS.get(), armor.get(EquipmentSlot.FEET.getIndex()));
    }

    private static void initEntities() {
        ApplicableUpgradesDB db = ApplicableUpgradesDB.getInstance();

        db.addApplicableUpgrades(ModEntityTypes.DRONE.get(), DRONE_UPGRADES);
        db.addApplicableUpgrades(ModEntityTypes.HARVESTING_DRONE.get(), BASIC_DRONE_UPGRADES);
        db.addApplicableUpgrades(ModEntityTypes.GUARD_DRONE.get(), GUARD_DRONE_UPGRADES);
        db.addApplicableUpgrades(ModEntityTypes.COLLECTOR_DRONE.get(), COLLECTOR_DRONE_UPGRADES);
        db.addApplicableUpgrades(ModEntityTypes.LOGISTICS_DRONE.get(), LOGISTICS_DRONE_UPGRADES);
    }

    private static void initTileEntities() {
        ApplicableUpgradesDB db = ApplicableUpgradesDB.getInstance();

        db.addApplicableUpgrades(ModBlockEntities.AIR_CANNON.get(), new Builder()
                .with(ModUpgrades.SECURITY.get(), 1)
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.RANGE.get(), 8)
                .with(ModUpgrades.ITEM_LIFE.get(), 8)
                .with(ModUpgrades.ENTITY_TRACKER.get(), 1)
                .with(ModUpgrades.BLOCK_TRACKER.get(), 1)
                .with(ModUpgrades.DISPENSER.get(), 1)
                .with(ModUpgrades.SPEED.get(), 10));
        db.addApplicableUpgrades(ModBlockEntities.AIR_COMPRESSOR.get(), new Builder()
                .with(ModUpgrades.SECURITY.get(), 1)
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.SPEED.get(), 10));
        db.addApplicableUpgrades(ModBlockEntities.ADVANCED_AIR_COMPRESSOR.get(), new Builder()
                .with(ModUpgrades.SECURITY.get(), 1)
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.SPEED.get(), 10));
        db.addApplicableUpgrades(ModBlockEntities.ASSEMBLY_CONTROLLER.get(), new Builder()
                .with(ModUpgrades.SECURITY.get(), 1)
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.SPEED.get(), 10));
        db.addApplicableUpgrades(ModBlockEntities.CHARGING_STATION.get(), new Builder()
                .with(ModUpgrades.SECURITY.get(), 1)
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.SPEED.get(), 10)
                .with(ModUpgrades.DISPENSER.get(), 1));
        db.addApplicableUpgrades(ModBlockEntities.ELEVATOR_BASE.get(), new Builder()
                .with(ModUpgrades.SECURITY.get(), 1)
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.SPEED.get(), 10)
                .with(ModUpgrades.CHARGING.get(), 4));
        db.addApplicableUpgrades(ModBlockEntities.PNEUMATIC_DOOR_BASE.get(), new Builder()
                .with(ModUpgrades.SECURITY.get(), 1)
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.SPEED.get(), 10)
                .with(ModUpgrades.RANGE.get(), 8));
        db.addApplicableUpgrades(ModBlockEntities.PRESSURE_CHAMBER_INTERFACE.get(), new Builder()
                .with(ModUpgrades.SPEED.get(), 10)
                .with(ModUpgrades.DISPENSER.get(), 1));
        db.addApplicableUpgrades(ModBlockEntities.PRESSURE_CHAMBER_VALVE.get(), new Builder()
                .with(ModUpgrades.SECURITY.get(), 1)
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME));
        db.addApplicableUpgrades(ModBlockEntities.VACUUM_PUMP.get(), new Builder()
                .with(ModUpgrades.SECURITY.get(), 1)
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.SPEED.get(), 10));
        db.addApplicableUpgrades(ModBlockEntities.UV_LIGHT_BOX.get(), new Builder()
                .with(ModUpgrades.SECURITY.get(), 1)
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.DISPENSER.get(), 1)
                .with(ModUpgrades.SPEED.get(), 10));
        db.addApplicableUpgrades(ModBlockEntities.SECURITY_STATION.get(), new Builder()
                .with(ModUpgrades.ENTITY_TRACKER.get(), 12)
                .with(ModUpgrades.SECURITY.get(), 64)
                .with(ModUpgrades.RANGE.get(), 14));
        db.addApplicableUpgrades(ModBlockEntities.AERIAL_INTERFACE.get(), new Builder()
                .with(ModUpgrades.SECURITY.get(), 1)
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.DISPENSER.get(), 1));
        db.addApplicableUpgrades(ModBlockEntities.ELECTROSTATIC_COMPRESSOR.get(), new Builder()
                .with(ModUpgrades.SECURITY.get(), 1)
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME));
        db.addApplicableUpgrades(ModBlockEntities.OMNIDIRECTIONAL_HOPPER.get(), new Builder()
                .with(ModUpgrades.SPEED.get(), 11)
                .with(ModUpgrades.CREATIVE.get(), 1)
                .with(ModUpgrades.ENTITY_TRACKER.get(), 1)
                .with(ModUpgrades.DISPENSER.get(), 1));
        db.addApplicableUpgrades(ModBlockEntities.LIQUID_HOPPER.get(), new Builder()
                .with(ModUpgrades.SPEED.get(), 11)
                .with(ModUpgrades.CREATIVE.get(), 1)
                .with(ModUpgrades.ENTITY_TRACKER.get(), 1)
                .with(ModUpgrades.DISPENSER.get(), 1));
        db.addApplicableUpgrades(ModBlockEntities.LIQUID_COMPRESSOR.get(), new Builder()
                .with(ModUpgrades.SECURITY.get(), 1)
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.SPEED.get(), 10));
        db.addApplicableUpgrades(ModBlockEntities.ADVANCED_LIQUID_COMPRESSOR.get(), new Builder()
                .with(ModUpgrades.SECURITY.get(), 1)
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.SPEED.get(), 10));
        db.addApplicableUpgrades(ModBlockEntities.PROGRAMMABLE_CONTROLLER.get(), new Builder()
                .with(ModUpgrades.SECURITY.get(), 1)
                .with(ModUpgrades.MAGNET.get(), 6)
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.SPEED.get(), 10)
                .with(ModUpgrades.INVENTORY.get(), 35));
        db.addApplicableUpgrades(ModBlockEntities.GAS_LIFT.get(), new Builder()
                .with(ModUpgrades.SECURITY.get(), 1)
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.SPEED.get(), 10)
                .with(ModUpgrades.DISPENSER.get(), 1));
        db.addApplicableUpgrades(ModBlockEntities.THERMOPNEUMATIC_PROCESSING_PLANT.get(), new Builder()
                .with(ModUpgrades.SECURITY.get(), 1)
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.DISPENSER.get(), 1));
        db.addApplicableUpgrades(ModBlockEntities.SENTRY_TURRET.get(), new Builder()
                .with(ModUpgrades.RANGE.get(), 16));
        db.addApplicableUpgrades(ModBlockEntities.FLUX_COMPRESSOR.get(), new Builder()
                .with(ModUpgrades.SECURITY.get(), 1)
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.SPEED.get(), 10));
        db.addApplicableUpgrades(ModBlockEntities.PNEUMATIC_DYNAMO.get(), new Builder()
                .with(ModUpgrades.SECURITY.get(), 1)
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.SPEED.get(), 10));
        db.addApplicableUpgrades(ModBlockEntities.THERMAL_COMPRESSOR.get(), new Builder()
                .with(ModUpgrades.SECURITY.get(), 1)
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME));
        db.addApplicableUpgrades(ModBlockEntities.TANK_SMALL.get(), new Builder()
                .with(ModUpgrades.SPEED.get(), 7)
                .with(ModUpgrades.DISPENSER.get(), 1));
        db.addApplicableUpgrades(ModBlockEntities.TANK_MEDIUM.get(), new Builder()
                .with(ModUpgrades.SPEED.get(), 7)
                .with(ModUpgrades.DISPENSER.get(), 1));
        db.addApplicableUpgrades(ModBlockEntities.TANK_LARGE.get(), new Builder()
                .with(ModUpgrades.SPEED.get(), 7)
                .with(ModUpgrades.DISPENSER.get(), 1));
        db.addApplicableUpgrades(ModBlockEntities.TANK_HUGE.get(), new Builder()
                .with(ModUpgrades.SPEED.get(), 7)
                .with(ModUpgrades.DISPENSER.get(), 1));
        db.addApplicableUpgrades(ModBlockEntities.SMART_CHEST.get(), new Builder()
                .with(ModUpgrades.SPEED.get(), 9)
                .with(ModUpgrades.DISPENSER.get(), 1)
                .with(ModUpgrades.MAGNET.get(), 1)
                .with(ModUpgrades.RANGE.get(), 4));
        db.addApplicableUpgrades(ModBlockEntities.FLUID_MIXER.get(), new Builder()
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.SECURITY.get(), 1)
                .with(ModUpgrades.DISPENSER.get(), 1));
        db.addApplicableUpgrades(ModBlockEntities.VACUUM_TRAP.get(), new Builder()
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.RANGE.get(), 6)
                .with(ModUpgrades.SECURITY.get(), 1));
        db.addApplicableUpgrades(ModBlockEntities.SPAWNER_EXTRACTOR.get(), new Builder()
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME));
        db.addApplicableUpgrades(ModBlockEntities.PRESSURIZED_SPAWNER.get(), new Builder()
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.RANGE.get(), 6)
                .with(ModUpgrades.SPEED.get(), 10)
                .with(ModUpgrades.SECURITY.get(), 1));
        db.addApplicableUpgrades(ModBlockEntities.SOLAR_COMPRESSOR.get(), new Builder()
                .with(ModUpgrades.SECURITY.get(), 1)
                .with(ModUpgrades.VOLUME.get(), MAX_VOLUME)
                .with(ModUpgrades.SPEED.get(), 10));

        // universal sensor needs some dynamic calculation...
        Builder sensorBuilder = new Builder();
        SensorHandler.getInstance().getUniversalSensorUpgrades().forEach(upgrade -> sensorBuilder.with(upgrade, 1));
        sensorBuilder.with(ModUpgrades.RANGE.get(), 64).with(ModUpgrades.SECURITY.get(), 1).with(ModUpgrades.VOLUME.get(), MAX_VOLUME);
        db.addApplicableUpgrades(ModBlockEntities.UNIVERSAL_SENSOR.get(), sensorBuilder);
    }

}
