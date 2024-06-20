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

import com.mojang.serialization.MapCodec;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.registry.PNCRegistries;
import me.desht.pneumaticcraft.common.drone.progwidgets.*;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.ProgWidgetCC;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModProgWidgetTypes {
    public static final DeferredRegister<ProgWidgetType<?>> PROG_WIDGETS_DEFERRED
            = DeferredRegister.create(PNCRegistries.PROG_WIDGETS_REGISTRY, Names.MOD_ID);

    public static final Supplier<ProgWidgetType<ProgWidgetComment>> COMMENT
            = register("comment", ProgWidgetComment::new, ProgWidgetComment.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetStart>> START
            = register("start", ProgWidgetStart::new, ProgWidgetStart.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetArea>> AREA
            = register("area", ProgWidgetArea::new, ProgWidgetArea.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetText>> TEXT
            = register("text", ProgWidgetText::new, ProgWidgetText.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetItemFilter>> ITEM_FILTER
            = register("item_filter", ProgWidgetItemFilter::new, ProgWidgetItemFilter.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetItemAssign>> ITEM_ASSIGN
            = register("item_assign", ProgWidgetItemAssign::new, ProgWidgetItemAssign.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetLiquidFilter>> LIQUID_FILTER
            = register("liquid_filter", ProgWidgetLiquidFilter::new, ProgWidgetLiquidFilter.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetCoordinate>> COORDINATE
            = register("coordinate", ProgWidgetCoordinate::new, ProgWidgetCoordinate.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetCoordinateOperator>> COORDINATE_OPERATOR
            = register("coordinate_operator", ProgWidgetCoordinateOperator::new, ProgWidgetCoordinateOperator.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetEntityAttack>> ENTITY_ATTACK
            = register("entity_attack", ProgWidgetEntityAttack::new, ProgWidgetEntityAttack.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetDig>> DIG
            = register("dig", ProgWidgetDig::new, ProgWidgetDig.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetHarvest>> HARVEST
            = register("harvest", ProgWidgetHarvest::new, ProgWidgetHarvest.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetPlace>> PLACE
            = register("place", ProgWidgetPlace::new, ProgWidgetPlace.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetBlockRightClick>> BLOCK_RIGHT_CLICK
            = register("block_right_click", ProgWidgetBlockRightClick::new, ProgWidgetBlockRightClick.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetEntityRightClick>> ENTITY_RIGHT_CLICK
            = register("entity_right_click", ProgWidgetEntityRightClick::new, ProgWidgetEntityRightClick.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetPickupItem>> PICKUP_ITEM
            = register("pickup_item", ProgWidgetPickupItem::new, ProgWidgetPickupItem.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetDropItem>> DROP_ITEM
            = register("drop_item", ProgWidgetDropItem::new, ProgWidgetDropItem.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetVoidItem>> VOID_ITEM
            = register("void_item", ProgWidgetVoidItem::new, ProgWidgetVoidItem.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetVoidLiquid>> VOID_FLUID
            = register("void_liquid", ProgWidgetVoidLiquid::new, ProgWidgetVoidLiquid.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetInventoryExport>> INVENTORY_EXPORT
            = register("inventory_export", ProgWidgetInventoryExport::new, ProgWidgetInventoryExport.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetInventoryImport>> INVENTORY_IMPORT
            = register("inventory_import", ProgWidgetInventoryImport::new, ProgWidgetInventoryImport.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetLiquidExport>> LIQUID_EXPORT
            = register("liquid_export", ProgWidgetLiquidExport::new, ProgWidgetLiquidExport.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetLiquidImport>> LIQUID_IMPORT
            = register("liquid_import", ProgWidgetLiquidImport::new, ProgWidgetLiquidImport.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetEntityExport>> ENTITY_EXPORT
            = register("entity_export", ProgWidgetEntityExport::new, ProgWidgetEntityExport.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetEntityImport>> ENTITY_IMPORT
            = register("entity_import", ProgWidgetEntityImport::new, ProgWidgetEntityImport.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetEnergyImport>> RF_IMPORT
            = register("rf_import", ProgWidgetEnergyImport::new, ProgWidgetEnergyImport.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetEnergyExport>> RF_EXPORT
            = register("rf_export", ProgWidgetEnergyExport::new, ProgWidgetEnergyExport.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetGoToLocation>> GOTO
            = register("goto", ProgWidgetGoToLocation::new, ProgWidgetGoToLocation.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetTeleport>> TELEPORT
            = register("teleport", ProgWidgetTeleport::new, ProgWidgetTeleport.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetEmitRedstone>> EMIT_REDSTONE
            = register("emit_redstone", ProgWidgetEmitRedstone::new, ProgWidgetEmitRedstone.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetLabel>> LABEL
            = register("label", ProgWidgetLabel::new, ProgWidgetLabel.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetJump>> JUMP
            = register("jump", ProgWidgetJump::new, ProgWidgetJump.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetJumpSub>> JUMP_SUB
            = register("jump_sub", ProgWidgetJumpSub::new, ProgWidgetJumpSub.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetWait>> WAIT
            = register("wait", ProgWidgetWait::new, ProgWidgetWait.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetRename>> RENAME
            = register("rename", ProgWidgetRename::new, ProgWidgetRename.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetSuicide>> SUICIDE
            = register("suicide", ProgWidgetSuicide::new, ProgWidgetSuicide.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetExternalProgram>> EXTERNAL_PROGRAM
            = register("external_program", ProgWidgetExternalProgram::new, ProgWidgetExternalProgram.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetCrafting>> CRAFTING
            = register("crafting", ProgWidgetCrafting::new, ProgWidgetCrafting.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetStandby>> STANDBY
            = register("standby", ProgWidgetStandby::new, ProgWidgetStandby.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetLogistics>> LOGISTICS
            = register("logistics", ProgWidgetLogistics::new, ProgWidgetLogistics.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetForEachCoordinate>> FOR_EACH_COORDINATE
            = register("for_each_coordinate", ProgWidgetForEachCoordinate::new, ProgWidgetForEachCoordinate.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetForEachItem>> FOR_EACH_ITEM
            = register("for_each_item", ProgWidgetForEachItem::new, ProgWidgetForEachItem.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetEditSign>> EDIT_SIGN
            = register("edit_sign", ProgWidgetEditSign::new, ProgWidgetEditSign.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetCoordinateCondition>> CONDITION_COORDINATE
            = register("condition_coordinate", ProgWidgetCoordinateCondition::new, ProgWidgetCoordinateCondition.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetRedstoneCondition>> CONDITION_REDSTONE
            = register("condition_redstone", ProgWidgetRedstoneCondition::new, ProgWidgetRedstoneCondition.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetLightCondition>> CONDITION_LIGHT
            = register("condition_light", ProgWidgetLightCondition::new, ProgWidgetLightCondition.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetItemInventoryCondition>> CONDITION_ITEM_INVENTORY
            = register("condition_item_inventory", ProgWidgetItemInventoryCondition::new, ProgWidgetItemInventoryCondition.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetBlockCondition>> CONDITION_BLOCK
            = register("condition_block", ProgWidgetBlockCondition::new, ProgWidgetBlockCondition.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetLiquidInventoryCondition>> CONDITION_LIQUID_INVENTORY
            = register("condition_liquid_inventory", ProgWidgetLiquidInventoryCondition::new, ProgWidgetLiquidInventoryCondition.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetEntityCondition>> CONDITION_ENTITY
            = register("condition_entity", ProgWidgetEntityCondition::new, ProgWidgetEntityCondition.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetPressureCondition>> CONDITION_PRESSURE
            = register("condition_pressure", ProgWidgetPressureCondition::new, ProgWidgetPressureCondition.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetItemCondition>> CONDITION_ITEM
            = register("condition_item", ProgWidgetItemCondition::new, ProgWidgetItemCondition.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetDroneConditionItem>> DRONE_CONDITION_ITEM
            = register("drone_condition_item", ProgWidgetDroneConditionItem::new, ProgWidgetDroneConditionItem.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetDroneConditionFluid>> DRONE_CONDITION_LIQUID
            = register("drone_condition_liquid", ProgWidgetDroneConditionFluid::new, ProgWidgetDroneConditionFluid.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetDroneConditionEntity>> DRONE_CONDITION_ENTITY
            = register("drone_condition_entity", ProgWidgetDroneConditionEntity::new, ProgWidgetDroneConditionEntity.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetDroneConditionPressure>> DRONE_CONDITION_PRESSURE
            = register("drone_condition_pressure", ProgWidgetDroneConditionPressure::new, ProgWidgetDroneConditionPressure.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetDroneConditionUpgrades>> DRONE_CONDITION_UPGRADES
            = register("drone_condition_upgrades", ProgWidgetDroneConditionUpgrades::new, ProgWidgetDroneConditionUpgrades.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetEnergyCondition>> CONDITION_RF
            = register("condition_rf", ProgWidgetEnergyCondition::new, ProgWidgetEnergyCondition.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetDroneConditionEnergy>> DRONE_CONDITION_RF
            = register("drone_condition_rf", ProgWidgetDroneConditionEnergy::new, ProgWidgetDroneConditionEnergy.CODEC);
    public static final Supplier<ProgWidgetType<ProgWidgetCC>> COMPUTER_CONTROL
            = register("computer_control", ProgWidgetCC::new, ProgWidgetCC.CODEC);


    private static <P extends IProgWidget, T extends ProgWidgetType<P>> Supplier<T> register(String name, Supplier<P> defaultSupplier, MapCodec<P> codec) {
        //noinspection unchecked
        return PROG_WIDGETS_DEFERRED.register(name, () -> (T) ProgWidgetType.createType(defaultSupplier, codec));
    }

}
