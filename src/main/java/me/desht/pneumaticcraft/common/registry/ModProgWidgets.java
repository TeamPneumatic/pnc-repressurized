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

package me.desht.pneumaticcraft.common.registry;

import me.desht.pneumaticcraft.api.drone.IProgWidgetBase;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.drone.progwidgets.*;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.ProgWidgetCC;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ModProgWidgets {
    public static final ResourceKey<Registry<ProgWidgetType<?>>> PROG_WIDGETS_KEY
            = ResourceKey.createRegistryKey(RL("prog_widgets"));
    public static final Registry<ProgWidgetType<?>> PROG_WIDGETS_REGISTRY
            = new RegistryBuilder<>(PROG_WIDGETS_KEY).create();

    public static final DeferredRegister<ProgWidgetType<?>> PROG_WIDGETS_DEFERRED
            = DeferredRegister.create(PROG_WIDGETS_REGISTRY, Names.MOD_ID);

    public static final Supplier<ProgWidgetType<ProgWidgetComment>> COMMENT
            = register("comment", ProgWidgetComment::new);
    public static final Supplier<ProgWidgetType<ProgWidgetStart>> START
            = register("start", ProgWidgetStart::new);
    public static final Supplier<ProgWidgetType<ProgWidgetArea>> AREA
            = register("area", ProgWidgetArea::new);
    public static final Supplier<ProgWidgetType<ProgWidgetText>> TEXT
            = register("text", ProgWidgetText::new);
    public static final Supplier<ProgWidgetType<ProgWidgetItemFilter>> ITEM_FILTER
            = register("item_filter", ProgWidgetItemFilter::new);
    public static final Supplier<ProgWidgetType<ProgWidgetItemAssign>> ITEM_ASSIGN
            = register("item_assign", ProgWidgetItemAssign::new);
    public static final Supplier<ProgWidgetType<ProgWidgetLiquidFilter>> LIQUID_FILTER
            = register("liquid_filter", ProgWidgetLiquidFilter::new);
    public static final Supplier<ProgWidgetType<ProgWidgetCoordinate>> COORDINATE
            = register("coordinate", ProgWidgetCoordinate::new);
    public static final Supplier<ProgWidgetType<ProgWidgetCoordinateOperator>> COORDINATE_OPERATOR
            = register("coordinate_operator", ProgWidgetCoordinateOperator::new);
    public static final Supplier<ProgWidgetType<ProgWidgetEntityAttack>> ENTITY_ATTACK
            = register("entity_attack", ProgWidgetEntityAttack::new);
    public static final Supplier<ProgWidgetType<ProgWidgetDig>> DIG
            = register("dig", ProgWidgetDig::new);
    public static final Supplier<ProgWidgetType<ProgWidgetHarvest>> HARVEST
            = register("harvest", ProgWidgetHarvest::new);
    public static final Supplier<ProgWidgetType<ProgWidgetPlace>> PLACE
            = register("place", ProgWidgetPlace::new);
    public static final Supplier<ProgWidgetType<ProgWidgetBlockRightClick>> BLOCK_RIGHT_CLICK
            = register("block_right_click", ProgWidgetBlockRightClick::new);
    public static final Supplier<ProgWidgetType<ProgWidgetEntityRightClick>> ENTITY_RIGHT_CLICK
            = register("entity_right_click", ProgWidgetEntityRightClick::new);
    public static final Supplier<ProgWidgetType<ProgWidgetPickupItem>> PICKUP_ITEM
            = register("pickup_item", ProgWidgetPickupItem::new);
    public static final Supplier<ProgWidgetType<ProgWidgetDropItem>> DROP_ITEM
            = register("drop_item", ProgWidgetDropItem::new);
    public static final Supplier<ProgWidgetType<ProgWidgetVoidItem>> VOID_ITEM
            = register("void_item", ProgWidgetVoidItem::new);
    public static final Supplier<ProgWidgetType<ProgWidgetVoidLiquid>> VOID_FLUID
            = register("void_liquid", ProgWidgetVoidLiquid::new);
    public static final Supplier<ProgWidgetType<ProgWidgetInventoryExport>> INVENTORY_EXPORT
            = register("inventory_export", ProgWidgetInventoryExport::new);
    public static final Supplier<ProgWidgetType<ProgWidgetInventoryImport>> INVENTORY_IMPORT
            = register("inventory_import", ProgWidgetInventoryImport::new);
    public static final Supplier<ProgWidgetType<ProgWidgetLiquidExport>> LIQUID_EXPORT
            = register("liquid_export", ProgWidgetLiquidExport::new);
    public static final Supplier<ProgWidgetType<ProgWidgetLiquidImport>> LIQUID_IMPORT
            = register("liquid_import", ProgWidgetLiquidImport::new);
    public static final Supplier<ProgWidgetType<ProgWidgetEntityExport>> ENTITY_EXPORT
            = register("entity_export", ProgWidgetEntityExport::new);
    public static final Supplier<ProgWidgetType<ProgWidgetEntityImport>> ENTITY_IMPORT
            = register("entity_import", ProgWidgetEntityImport::new);
    public static final Supplier<ProgWidgetType<ProgWidgetEnergyImport>> RF_IMPORT
            = register("rf_import", ProgWidgetEnergyImport::new);
    public static final Supplier<ProgWidgetType<ProgWidgetEnergyExport>> RF_EXPORT
            = register("rf_export", ProgWidgetEnergyExport::new);
    public static final Supplier<ProgWidgetType<ProgWidgetGoToLocation>> GOTO
            = register("goto", ProgWidgetGoToLocation::new);
    public static final Supplier<ProgWidgetType<ProgWidgetTeleport>> TELEPORT
            = register("teleport", ProgWidgetTeleport::new);
    public static final Supplier<ProgWidgetType<ProgWidgetEmitRedstone>> EMIT_REDSTONE
            = register("emit_redstone", ProgWidgetEmitRedstone::new);
    public static final Supplier<ProgWidgetType<ProgWidgetLabel>> LABEL
            = register("label", ProgWidgetLabel::new);
    public static final Supplier<ProgWidgetType<ProgWidgetJump>> JUMP
            = register("jump", ProgWidgetJump::new);
    public static final Supplier<ProgWidgetType<ProgWidgetJumpSub>> JUMP_SUB
            = register("jump_sub", ProgWidgetJumpSub::new);
    public static final Supplier<ProgWidgetType<ProgWidgetWait>> WAIT
            = register("wait", ProgWidgetWait::new);
    public static final Supplier<ProgWidgetType<ProgWidgetRename>> RENAME
            = register("rename", ProgWidgetRename::new);
    public static final Supplier<ProgWidgetType<ProgWidgetSuicide>> SUICIDE
            = register("suicide", ProgWidgetSuicide::new);
    public static final Supplier<ProgWidgetType<ProgWidgetExternalProgram>> EXTERNAL_PROGRAM
            = register("external_program", ProgWidgetExternalProgram::new);
    public static final Supplier<ProgWidgetType<ProgWidgetCrafting>> CRAFTING
            = register("crafting", ProgWidgetCrafting::new);
    public static final Supplier<ProgWidgetType<ProgWidgetStandby>> STANDBY
            = register("standby", ProgWidgetStandby::new);
    public static final Supplier<ProgWidgetType<ProgWidgetLogistics>> LOGISTICS
            = register("logistics", ProgWidgetLogistics::new);
    public static final Supplier<ProgWidgetType<ProgWidgetForEachCoordinate>> FOR_EACH_COORDINATE
            = register("for_each_coordinate", ProgWidgetForEachCoordinate::new);
    public static final Supplier<ProgWidgetType<ProgWidgetForEachItem>> FOR_EACH_ITEM
            = register("for_each_item", ProgWidgetForEachItem::new);
    public static final Supplier<ProgWidgetType<ProgWidgetEditSign>> EDIT_SIGN
            = register("edit_sign", ProgWidgetEditSign::new);
    public static final Supplier<ProgWidgetType<ProgWidgetCoordinateCondition>> CONDITION_COORDINATE
            = register("condition_coordinate", ProgWidgetCoordinateCondition::new);
    public static final Supplier<ProgWidgetType<ProgWidgetRedstoneCondition>> CONDITION_REDSTONE
            = register("condition_redstone", ProgWidgetRedstoneCondition::new);
    public static final Supplier<ProgWidgetType<ProgWidgetLightCondition>> CONDITION_LIGHT
            = register("condition_light", ProgWidgetLightCondition::new);
    public static final Supplier<ProgWidgetType<ProgWidgetItemInventoryCondition>> CONDITION_ITEM_INVENTORY
            = register("condition_item_inventory", ProgWidgetItemInventoryCondition::new);
    public static final Supplier<ProgWidgetType<ProgWidgetBlockCondition>> CONDITION_BLOCK
            = register("condition_block", ProgWidgetBlockCondition::new);
    public static final Supplier<ProgWidgetType<ProgWidgetLiquidInventoryCondition>> CONDITION_LIQUID_INVENTORY
            = register("condition_liquid_inventory", ProgWidgetLiquidInventoryCondition::new);
    public static final Supplier<ProgWidgetType<ProgWidgetEntityCondition>> CONDITION_ENTITY
            = register("condition_entity", ProgWidgetEntityCondition::new);
    public static final Supplier<ProgWidgetType<ProgWidgetPressureCondition>> CONDITION_PRESSURE
            = register("condition_pressure", ProgWidgetPressureCondition::new);
    public static final Supplier<ProgWidgetType<ProgWidgetItemCondition>> CONDITION_ITEM
            = register("condition_item", ProgWidgetItemCondition::new);
    public static final Supplier<ProgWidgetType<ProgWidgetDroneConditionItem>> DRONE_CONDITION_ITEM
            = register("drone_condition_item", ProgWidgetDroneConditionItem::new);
    public static final Supplier<ProgWidgetType<ProgWidgetDroneConditionFluid>> DRONE_CONDITION_LIQUID
            = register("drone_condition_liquid", ProgWidgetDroneConditionFluid::new);
    public static final Supplier<ProgWidgetType<ProgWidgetDroneConditionEntity>> DRONE_CONDITION_ENTITY
            = register("drone_condition_entity", ProgWidgetDroneConditionEntity::new);
    public static final Supplier<ProgWidgetType<ProgWidgetDroneConditionPressure>> DRONE_CONDITION_PRESSURE
            = register("drone_condition_pressure", ProgWidgetDroneConditionPressure::new);
    public static final Supplier<ProgWidgetType<ProgWidgetDroneConditionUpgrades>> DRONE_CONDITION_UPGRADES
            = register("drone_condition_upgrades", ProgWidgetDroneConditionUpgrades::new);
    public static final Supplier<ProgWidgetType<ProgWidgetEnergyCondition>> CONDITION_RF
            = register("condition_rf", ProgWidgetEnergyCondition::new);
    public static final Supplier<ProgWidgetType<ProgWidgetDroneConditionEnergy>> DRONE_CONDITION_RF
            = register("drone_condition_rf", ProgWidgetDroneConditionEnergy::new);
    public static final Supplier<ProgWidgetType<ProgWidgetCC>> COMPUTER_CONTROL
            = register("computer_control", ProgWidgetCC::new);

    private static <P extends IProgWidgetBase, T extends ProgWidgetType<P>> Supplier<T> register(String name, Supplier<P> sup) {
        //noinspection unchecked
        return PROG_WIDGETS_DEFERRED.register(name, () -> (T) ProgWidgetType.createType(sup));
    }
}
