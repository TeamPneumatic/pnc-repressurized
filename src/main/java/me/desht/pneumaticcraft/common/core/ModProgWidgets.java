package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.progwidgets.*;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

import java.util.ArrayList;
import java.util.List;
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
    public static final ProgWidgetType<ProgWidgetItemCondition> CONDTION_ITEM = null;
    public static final ProgWidgetType<ProgWidgetDroneConditionItem> DRONE_CONDITION_ITEM = null;
    public static final ProgWidgetType<ProgWidgetDroneConditionFluid> DRONE_CONDITION_LIQUID = null;
    public static final ProgWidgetType<ProgWidgetDroneConditionEntity> DRONE_CONDITION_ENTITY = null;
    public static final ProgWidgetType<ProgWidgetDroneConditionPressure> DRONE_CONDITION_PRESSURE = null;
    public static final ProgWidgetType<ProgWidgetEnergyCondition> CONDITION_RF = null;
    public static final ProgWidgetType<ProgWidgetDroneConditionEnergy> DRONE_CONDITION_RF = null;
    public static final ProgWidgetType<ProgWidgetCustomBlockInteract> CUSTOM_BLOCK_INTERACT = null;

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        // maintain an ordered list for the programmer GUI
        // the widget tray will show the widgets in the order they are registered below
        public static final List<ProgWidgetType<?>> WIDGET_LIST = new ArrayList<>();

        @SubscribeEvent
        public static void register(RegistryEvent.Register<ProgWidgetType<?>> event) {
            IForgeRegistry<ProgWidgetType<?>> r = event.getRegistry();
            
            register(r, ProgWidgetComment::new, "comment");
            register(r, ProgWidgetStart::new, "start");
            register(r, ProgWidgetArea::new, "area");
            register(r, ProgWidgetText::new, "text");
            register(r, ProgWidgetItemFilter::new, "item_filter");
            register(r, ProgWidgetItemAssign::new, "item_assign");
            register(r, ProgWidgetLiquidFilter::new, "liquid_filter");
            register(r, ProgWidgetCoordinate::new, "coordinate");
            register(r, ProgWidgetCoordinateOperator::new, "coordinate_operator");
            register(r, ProgWidgetEntityAttack::new, "entity_attack");
            register(r, ProgWidgetDig::new, "dig");
            register(r, ProgWidgetHarvest::new, "harvest");
            register(r, ProgWidgetPlace::new, "place");
            register(r, ProgWidgetBlockRightClick::new, "block_right_click");
            register(r, ProgWidgetEntityRightClick::new, "entity_right_click");
            register(r, ProgWidgetPickupItem::new, "pickup_item");
            register(r, ProgWidgetDropItem::new, "drop_item");
            register(r, ProgWidgetInventoryExport::new, "inventory_export");
            register(r, ProgWidgetInventoryImport::new, "inventory_import");
            register(r, ProgWidgetLiquidExport::new, "liquid_export");
            register(r, ProgWidgetLiquidImport::new, "liquid_import");
            register(r, ProgWidgetEntityExport::new, "entity_export");
            register(r, ProgWidgetEntityImport::new, "entity_import");
            register(r, ProgWidgetEnergyImport::new, "rf_import");
            register(r, ProgWidgetEnergyExport::new, "rf_export");
            register(r, ProgWidgetGoToLocation::new, "goto");
            register(r, ProgWidgetTeleport::new, "teleport");
            register(r, ProgWidgetEmitRedstone::new, "emit_redstone");
            register(r, ProgWidgetLabel::new, "label");
            register(r, ProgWidgetJump::new, "jump");
            register(r, ProgWidgetWait::new, "wait");
            register(r, ProgWidgetRename::new, "rename");
            register(r, ProgWidgetSuicide::new, "suicide");
            register(r, ProgWidgetExternalProgram::new, "external_program");
            register(r, ProgWidgetCrafting::new, "crafting");
            register(r, ProgWidgetStandby::new, "standby");
            register(r, ProgWidgetLogistics::new, "logistics");
            register(r, ProgWidgetForEachCoordinate::new, "for_each_coordinate");
            register(r, ProgWidgetForEachItem::new, "for_each_item");
            register(r, ProgWidgetEditSign::new, "edit_sign");
            register(r, ProgWidgetCoordinateCondition::new, "condition_coordinate");
            register(r, ProgWidgetRedstoneCondition::new, "condition_redstone");
            register(r, ProgWidgetLightCondition::new, "condition_light");
            register(r, ProgWidgetItemInventoryCondition::new, "condition_item_inventory");
            register(r, ProgWidgetBlockCondition::new, "condition_block");
            register(r, ProgWidgetLiquidInventoryCondition::new, "condition_liquid_inventory");
            register(r, ProgWidgetEntityCondition::new, "condition_entity");
            register(r, ProgWidgetPressureCondition::new, "condition_pressure");
            register(r, ProgWidgetItemCondition::new, "condtion_item");
            register(r, ProgWidgetDroneConditionItem::new, "drone_condition_item");
            register(r, ProgWidgetDroneConditionFluid::new, "drone_condition_liquid");
            register(r, ProgWidgetDroneConditionEntity::new, "drone_condition_entity");
            register(r, ProgWidgetDroneConditionPressure::new, "drone_condition_pressure");
            register(r, ProgWidgetEnergyCondition::new, "condition_rf");
            register(r, ProgWidgetDroneConditionEnergy::new, "drone_condition_rf");
        }

        public static void register(IForgeRegistry<ProgWidgetType<?>> r, Supplier<? extends IProgWidget> sup, ResourceLocation registryName) {
            ProgWidgetType<?> type = new ProgWidgetType<>(sup).setRegistryName(registryName);
            r.register(type);
            WIDGET_LIST.add(type);
        }

        private static void register(IForgeRegistry<ProgWidgetType<?>> r, Supplier<? extends IProgWidget> sup, String registryName) {
            register(r, sup, RL(registryName));
        }
    }
}
