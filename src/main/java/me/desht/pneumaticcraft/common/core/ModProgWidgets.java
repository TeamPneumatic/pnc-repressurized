package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.drone.IProgWidgetBase;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.progwidgets.*;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@ObjectHolder(Names.MOD_ID)
public class ModProgWidgets {
    public static final ProgWidgetType<ProgWidgetComment> COMMENT = null;
    public static final ProgWidgetType<ProgWidgetStart> START = null;
    public static final ProgWidgetType<ProgWidgetArea> AREA = null;
    public static final ProgWidgetType<ProgWidgetText> TEXT = null;
    public static final ProgWidgetType<ProgWidgetItemFilter> ITEM_FILTER = null;
    public static final ProgWidgetType<ProgWidgetItemAssign> ITEM_ASSIGN = null;
    public static final ProgWidgetType<ProgWidgetLiquidFilter> LIQUID_FILTER = null;
    public static final ProgWidgetType<ProgWidgetCoordinate> COORDINATE = null;
    public static final ProgWidgetType<ProgWidgetCoordinateOperator> COORDINATE_OPERATOR = null;
    public static final ProgWidgetType<ProgWidgetEntityAttack> ENTITY_ATTACK = null;
    public static final ProgWidgetType<ProgWidgetDig> DIG = null;
    public static final ProgWidgetType<ProgWidgetHarvest> HARVEST = null;
    public static final ProgWidgetType<ProgWidgetPlace> PLACE = null;
    public static final ProgWidgetType<ProgWidgetBlockRightClick> BLOCK_RIGHT_CLICK = null;
    public static final ProgWidgetType<ProgWidgetEntityRightClick> ENTITY_RIGHT_CLICK = null;
    public static final ProgWidgetType<ProgWidgetPickupItem> PICKUP_ITEM = null;
    public static final ProgWidgetType<ProgWidgetDropItem> DROP_ITEM = null;
    public static final ProgWidgetType<ProgWidgetInventoryExport> INVENTORY_EXPORT = null;
    public static final ProgWidgetType<ProgWidgetInventoryImport> INVENTORY_IMPORT = null;
    public static final ProgWidgetType<ProgWidgetLiquidExport> LIQUID_EXPORT = null;
    public static final ProgWidgetType<ProgWidgetLiquidImport> LIQUID_IMPORT = null;
    public static final ProgWidgetType<ProgWidgetEntityExport> ENTITY_EXPORT = null;
    public static final ProgWidgetType<ProgWidgetEntityImport> ENTITY_IMPORT = null;
    public static final ProgWidgetType<ProgWidgetEnergyImport> RF_IMPORT = null;
    public static final ProgWidgetType<ProgWidgetEnergyExport> RF_EXPORT = null;
    public static final ProgWidgetType<ProgWidgetGoToLocation> GOTO = null;
    public static final ProgWidgetType<ProgWidgetTeleport> TELEPORT = null;
    public static final ProgWidgetType<ProgWidgetEmitRedstone> EMIT_REDSTONE = null;
    public static final ProgWidgetType<ProgWidgetLabel> LABEL = null;
    public static final ProgWidgetType<ProgWidgetJump> JUMP = null;
    public static final ProgWidgetType<ProgWidgetWait> WAIT = null;
    public static final ProgWidgetType<ProgWidgetRename> RENAME = null;
    public static final ProgWidgetType<ProgWidgetSuicide> SUICIDE = null;
    public static final ProgWidgetType<ProgWidgetExternalProgram> EXTERNAL_PROGRAM = null;
    public static final ProgWidgetType<ProgWidgetCrafting> CRAFTING = null;
    public static final ProgWidgetType<ProgWidgetStandby> STANDBY = null;
    public static final ProgWidgetType<ProgWidgetLogistics> LOGISTICS = null;
    public static final ProgWidgetType<ProgWidgetForEachCoordinate> FOR_EACH_COORDINATE = null;
    public static final ProgWidgetType<ProgWidgetForEachItem> FOR_EACH_ITEM = null;
    public static final ProgWidgetType<ProgWidgetEditSign> EDIT_SIGN = null;
    public static final ProgWidgetType<ProgWidgetCoordinateCondition> CONDITION_COORDINATE = null;
    public static final ProgWidgetType<ProgWidgetRedstoneCondition> CONDITION_REDSTONE = null;
    public static final ProgWidgetType<ProgWidgetLightCondition> CONDITION_LIGHT = null;
    public static final ProgWidgetType<ProgWidgetItemInventoryCondition> CONDITION_ITEM_INVENTORY = null;
    public static final ProgWidgetType<ProgWidgetBlockCondition> CONDITION_BLOCK = null;
    public static final ProgWidgetType<ProgWidgetLiquidInventoryCondition> CONDITION_LIQUID_INVENTORY = null;
    public static final ProgWidgetType<ProgWidgetEntityCondition> CONDITION_ENTITY = null;
    public static final ProgWidgetType<ProgWidgetPressureCondition> CONDITION_PRESSURE = null;
    public static final ProgWidgetType<ProgWidgetItemCondition> CONDITION_ITEM = null;
    public static final ProgWidgetType<ProgWidgetDroneConditionItem> DRONE_CONDITION_ITEM = null;
    public static final ProgWidgetType<ProgWidgetDroneConditionFluid> DRONE_CONDITION_LIQUID = null;
    public static final ProgWidgetType<ProgWidgetDroneConditionEntity> DRONE_CONDITION_ENTITY = null;
    public static final ProgWidgetType<ProgWidgetDroneConditionPressure> DRONE_CONDITION_PRESSURE = null;
    public static final ProgWidgetType<ProgWidgetEnergyCondition> CONDITION_RF = null;
    public static final ProgWidgetType<ProgWidgetDroneConditionEnergy> DRONE_CONDITION_RF = null;

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Listener {
        @SubscribeEvent
        public static void register(RegistryEvent.Register<ProgWidgetType<?>> event) {
            IForgeRegistry<ProgWidgetType<?>> r = event.getRegistry();

            register(r, "comment", ProgWidgetComment::new);
            register(r, "start", ProgWidgetStart::new);
            register(r, "area", ProgWidgetArea::new);
            register(r, "text", ProgWidgetText::new);
            register(r, "item_filter", ProgWidgetItemFilter::new);
            register(r, "item_assign", ProgWidgetItemAssign::new);
            register(r, "liquid_filter", ProgWidgetLiquidFilter::new);
            register(r, "coordinate", ProgWidgetCoordinate::new);
            register(r, "coordinate_operator", ProgWidgetCoordinateOperator::new);
            register(r, "entity_attack", ProgWidgetEntityAttack::new);
            register(r, "dig", ProgWidgetDig::new);
            register(r, "harvest", ProgWidgetHarvest::new);
            register(r, "place", ProgWidgetPlace::new);
            register(r, "block_right_click", ProgWidgetBlockRightClick::new);
            register(r, "entity_right_click", ProgWidgetEntityRightClick::new);
            register(r, "pickup_item", ProgWidgetPickupItem::new);
            register(r, "drop_item", ProgWidgetDropItem::new);
            register(r, "inventory_export", ProgWidgetInventoryExport::new);
            register(r, "inventory_import", ProgWidgetInventoryImport::new);
            register(r, "liquid_export", ProgWidgetLiquidExport::new);
            register(r, "liquid_import", ProgWidgetLiquidImport::new);
            register(r, "entity_export", ProgWidgetEntityExport::new);
            register(r, "entity_import", ProgWidgetEntityImport::new);
            register(r, "rf_import", ProgWidgetEnergyImport::new);
            register(r, "rf_export", ProgWidgetEnergyExport::new);
            register(r, "goto", ProgWidgetGoToLocation::new);
            register(r, "teleport", ProgWidgetTeleport::new);
            register(r, "emit_redstone", ProgWidgetEmitRedstone::new);
            register(r, "label", ProgWidgetLabel::new);
            register(r, "jump", ProgWidgetJump::new);
            register(r, "wait", ProgWidgetWait::new);
            register(r, "rename", ProgWidgetRename::new);
            register(r, "suicide", ProgWidgetSuicide::new);
            register(r, "external_program", ProgWidgetExternalProgram::new);
            register(r, "crafting", ProgWidgetCrafting::new);
            register(r, "standby", ProgWidgetStandby::new);
            register(r, "logistics", ProgWidgetLogistics::new);
            register(r, "for_each_coordinate", ProgWidgetForEachCoordinate::new);
            register(r, "for_each_item", ProgWidgetForEachItem::new);
            register(r, "edit_sign", ProgWidgetEditSign::new);
            register(r, "condition_coordinate", ProgWidgetCoordinateCondition::new);
            register(r, "condition_redstone", ProgWidgetRedstoneCondition::new);
            register(r, "condition_light", ProgWidgetLightCondition::new);
            register(r, "condition_item_inventory", ProgWidgetItemInventoryCondition::new);
            register(r, "condition_block", ProgWidgetBlockCondition::new);
            register(r, "condition_liquid_inventory", ProgWidgetLiquidInventoryCondition::new);
            register(r, "condition_entity", ProgWidgetEntityCondition::new);
            register(r, "condition_pressure", ProgWidgetPressureCondition::new);
            register(r, "condition_item", ProgWidgetItemCondition::new);
            register(r, "drone_condition_item", ProgWidgetDroneConditionItem::new);
            register(r, "drone_condition_liquid", ProgWidgetDroneConditionFluid::new);
            register(r, "drone_condition_entity", ProgWidgetDroneConditionEntity::new);
            register(r, "drone_condition_pressure", ProgWidgetDroneConditionPressure::new);
            register(r, "condition_rf", ProgWidgetEnergyCondition::new);
            register(r, "drone_condition_rf", ProgWidgetDroneConditionEnergy::new);
        }

        public static void register(IForgeRegistry<ProgWidgetType<?>> registry, String name, Supplier<? extends IProgWidgetBase> sup) {
            registry.register(new ProgWidgetType<>(sup).setRegistryName(RL(name)));
        }
    }
}
