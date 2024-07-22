package me.desht.pneumaticcraft.lib;

import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.resources.ResourceLocation;

public class Textures {
    static final String ICON_LOCATION = Names.MOD_ID + ":";

    private static final String MODEL_LOCATION = ICON_LOCATION + "textures/pnc_model/";
    private static final String TUBE_MODULE_MODEL_LOCATION = MODEL_LOCATION + "modules/";
    private static final String GUI_LOCATION = ICON_LOCATION + "textures/gui/";
    private static final String ENTITY_LOCATION = ICON_LOCATION + "textures/entity/";
    private static final String DRONE_LOCATION = ICON_LOCATION + "textures/entity/drone/";
    private static final String ARMOR_LOCATION = ICON_LOCATION + "textures/armor/";
    private static final String RENDER_LOCATION = ICON_LOCATION + "textures/render/";
    private static final String PROG_WIDGET_LOCATION = ICON_LOCATION + "textures/progwidgets/";

    public static final String ARMOR_PNEUMATIC = ARMOR_LOCATION + "pneumatic";
    public static final String ARMOR_COMPRESSED_IRON = ARMOR_LOCATION + "compressed_iron";

    // Entity & TESR model textures
    public static final ResourceLocation MODEL_AIR_CANNON = modelTexture("air_cannon.png");
    public static final ResourceLocation MODEL_ELEVATOR = modelTexture("elevator.png");
    public static final ResourceLocation MODEL_PRESSURE_CHAMBER_INTERFACE = modelTexture("pressure_chamber_interface.png");
    public static final ResourceLocation MODEL_VACUUM_PUMP = modelTexture("vacuum_pump.png");
    public static final ResourceLocation MODEL_PNEUMATIC_DOOR_DYNAMIC = modelTexture("pneumatic_door_dynamic.png");
    public static final ResourceLocation MODEL_PNEUMATIC_DOOR_BASE = modelTexture("pneumatic_door_base.png");
    public static final ResourceLocation MODEL_ASSEMBLY_IO_EXPORT = modelTexture("assembly_io_export.png");
    public static final ResourceLocation MODEL_ASSEMBLY_IO_IMPORT = modelTexture("assembly_io_import.png");
    public static final ResourceLocation MODEL_ASSEMBLY_LASER_AND_DRILL = modelTexture("assembly_laser_and_drill.png");
    public static final ResourceLocation MODEL_ASSEMBLY_PLATFORM = modelTexture("assembly_platform.png");
    public static final ResourceLocation MODEL_ASSEMBLY_CONTROLLER = modelTexture("assembly_controller.png");
    public static final ResourceLocation MODEL_UNIVERSAL_SENSOR = modelTexture("universal_sensor.png");
    public static final ResourceLocation MODEL_DRONE_MINIGUN = modelTexture("drone_minigun.png");
    public static final ResourceLocation MODEL_LOGISTICS_FRAME_ACTIVE_PROVIDER = modelTexture("logistics_frame_active_provider.png");
    public static final ResourceLocation MODEL_LOGISTICS_FRAME_PASSIVE_PROVIDER = modelTexture("logistics_frame_passive_provider.png");
    public static final ResourceLocation MODEL_LOGISTICS_FRAME_DEFAULT_STORAGE = modelTexture("logistics_frame_default_storage.png");
    public static final ResourceLocation MODEL_LOGISTICS_FRAME_STORAGE = modelTexture("logistics_frame_storage.png");
    public static final ResourceLocation MODEL_LOGISTICS_FRAME_REQUESTER = modelTexture("logistics_frame_requester.png");
    public static final ResourceLocation MODEL_MANUAL_COMPRESSOR = modelTexture("manual_compressor.png");
    public static final ResourceLocation MODEL_CROP_SUPPORT = modelTexture("crop_support.png");
    public static final ResourceLocation MODEL_SPAWNER_EXTRACTOR = modelTexture("spawner_extractor.png");
    public static final ResourceLocation MODEL_SPAWNER_AGITATOR = modelTexture("spawner_agitator.png");
    public static final ResourceLocation MODEL_TRANSFER_GADGET_IN = modelTexture("transfer_gadget_in.png");
    public static final ResourceLocation MODEL_TRANSFER_GADGET_OUT = modelTexture("transfer_gadget_out.png");
    public static final ResourceLocation MODEL_SOLAR_COMPRESSOR = modelTexture("solar_compressor.png");

    // Tube Module textures
    public static final ResourceLocation MODEL_FLOW_DETECTOR = tubeModuleTexture("flow_detector.png");
    public static final ResourceLocation MODEL_LOGISTICS_MODULE = tubeModuleTexture("logistics_module.png");
    public static final ResourceLocation MODEL_GAUGE = tubeModuleTexture("gauge_module.png");
    public static final ResourceLocation MODEL_GAUGE_UPGRADED = tubeModuleTexture("gauge_module_upgraded.png");
    public static final ResourceLocation MODEL_AIR_GRATE = tubeModuleTexture("air_grate.png");
    public static final ResourceLocation MODEL_AIR_GRATE_UPGRADED = tubeModuleTexture("air_grate_upgraded.png");
    public static final ResourceLocation MODEL_CHARGING_MODULE = tubeModuleTexture("charging_module.png");
    public static final ResourceLocation MODEL_CHARGING_MODULE_UPGRADED = tubeModuleTexture("charging_module_upgraded.png");
    public static final ResourceLocation MODEL_SAFETY_VALVE = tubeModuleTexture("safety_valve.png");
    public static final ResourceLocation MODEL_SAFETY_VALVE_UPGRADED = tubeModuleTexture("safety_valve_upgraded.png");
    public static final ResourceLocation MODEL_REGULATOR_MODULE = tubeModuleTexture("regulator.png");
    public static final ResourceLocation MODEL_REGULATOR_MODULE_UPGRADED = tubeModuleTexture("regulator_upgraded.png");
    public static final ResourceLocation MODEL_REDSTONE_MODULE = tubeModuleTexture("redstone.png");
    public static final ResourceLocation MODEL_REDSTONE_MODULE_UPGRADED = tubeModuleTexture("redstone_upgraded.png");
    public static final ResourceLocation MODEL_VACUUM_MODULE = tubeModuleTexture("vacuum.png");
    public static final ResourceLocation MODEL_VACUUM_MODULE_UPGRADED = tubeModuleTexture("vacuum_upgraded.png");
    public static final ResourceLocation MODEL_THERMOSTAT_MODULE = tubeModuleTexture("thermostat_module.png");
    public static final ResourceLocation MODEL_THERMOSTAT_MODULE_UPGRADED = tubeModuleTexture("thermostat_module_upgraded.png");

    // Progwidget textures
    public static final ResourceLocation PROG_WIDGET_COMMENT = progWidgetTexture("comment_piece.png");
    public static final ResourceLocation PROG_WIDGET_AREA = progWidgetTexture("area_piece.png");
    public static final ResourceLocation PROG_WIDGET_ATTACK = progWidgetTexture("attack_piece.png");
    public static final ResourceLocation PROG_WIDGET_CC = progWidgetTexture("computer_control_piece.png");
    public static final ResourceLocation PROG_WIDGET_DIG = progWidgetTexture("dig_piece.png");
    public static final ResourceLocation PROG_WIDGET_HARVEST = progWidgetTexture("harvest_piece.png");
    public static final ResourceLocation PROG_WIDGET_GOTO = progWidgetTexture("goto_piece.png");
    public static final ResourceLocation PROG_WIDGET_TELEPORT = progWidgetTexture("teleport_piece.png");
    public static final ResourceLocation PROG_WIDGET_INV_EX = progWidgetTexture("inventory_export_piece.png");
    public static final ResourceLocation PROG_WIDGET_INV_IM = progWidgetTexture("inventory_import_piece.png");
    public static final ResourceLocation PROG_WIDGET_LIQUID_EX = progWidgetTexture("liquid_export_piece.png");
    public static final ResourceLocation PROG_WIDGET_LIQUID_IM = progWidgetTexture("liquid_import_piece.png");
    public static final ResourceLocation PROG_WIDGET_ENTITY_EX = progWidgetTexture("entity_export_piece.png");
    public static final ResourceLocation PROG_WIDGET_ENTITY_IM = progWidgetTexture("entity_import_piece.png");
    public static final ResourceLocation PROG_WIDGET_RF_EXPORT = progWidgetTexture("rf_export_piece.png");
    public static final ResourceLocation PROG_WIDGET_RF_IMPORT = progWidgetTexture("rf_import_piece.png");
//    public static final ResourceLocation PROG_WIDGET_ESSENTIA_EX = progWidgetTexture("essentia_export_piece.png");
//    public static final ResourceLocation PROG_WIDGET_ESSENTIA_IM = progWidgetTexture("essentia_import_piece.png");
//    public static final ResourceLocation PROG_WIDGET_ESSENTIA_FILTER = progWidgetTexture("essentia_filter_piece.png");
    public static final ResourceLocation PROG_WIDGET_PICK_ITEM = progWidgetTexture("item_pick_piece.png");
    public static final ResourceLocation PROG_WIDGET_ENTITY_RIGHT_CLICK = progWidgetTexture("entity_right_click_piece.png");
    public static final ResourceLocation PROG_WIDGET_BLOCK_RIGHT_CLICK = progWidgetTexture("block_right_click_piece.png");
    public static final ResourceLocation PROG_WIDGET_ITEM_FILTER = progWidgetTexture("item_filter_piece.png");
    public static final ResourceLocation PROG_WIDGET_LIQUID_FILTER = progWidgetTexture("liquid_filter_piece.png");
    public static final ResourceLocation PROG_WIDGET_PLACE = progWidgetTexture("place_piece.png");
    public static final ResourceLocation PROG_WIDGET_START = progWidgetTexture("start_piece.png");
    public static final ResourceLocation PROG_WIDGET_TEXT = progWidgetTexture("text_piece.png");
    public static final ResourceLocation PROG_WIDGET_LABEL = progWidgetTexture("label_piece.png");
    public static final ResourceLocation PROG_WIDGET_JUMP = progWidgetTexture("jump_piece.png");
    public static final ResourceLocation PROG_WIDGET_JUMP_SUB = progWidgetTexture("jump_sub_piece.png");
    public static final ResourceLocation PROG_WIDGET_WAIT = progWidgetTexture("wait_piece.png");
    public static final ResourceLocation PROG_WIDGET_DROP_ITEM = progWidgetTexture("item_drop_piece.png");
    public static final ResourceLocation PROG_WIDGET_EMIT_REDSTONE = progWidgetTexture("emit_redstone_piece.png");
    public static final ResourceLocation PROG_WIDGET_RENAME = progWidgetTexture("rename_piece.png");
    public static final ResourceLocation PROG_WIDGET_SUICIDE = progWidgetTexture("suicide_piece.png");
    public static final ResourceLocation PROG_WIDGET_EXTERNAL_PROGRAM = progWidgetTexture("external_program_piece.png");
    public static final ResourceLocation PROG_WIDGET_CRAFTING = progWidgetTexture("craft_piece.png");
    public static final ResourceLocation PROG_WIDGET_STANDBY = progWidgetTexture("standby_piece.png");
    public static final ResourceLocation PROG_WIDGET_COORDINATE = progWidgetTexture("coordinate_piece.png");
    public static final ResourceLocation PROG_WIDGET_ITEM_ASSIGN = progWidgetTexture("item_assign_piece.png");
    public static final ResourceLocation PROG_WIDGET_LOGISTICS = progWidgetTexture("logistics_piece.png");
    public static final ResourceLocation PROG_WIDGET_FOR_EACH_COORDINATE = progWidgetTexture("for_each_coordinate.png");
    public static final ResourceLocation PROG_WIDGET_FOR_EACH_ITEM = progWidgetTexture("for_each_item.png");
    public static final ResourceLocation PROG_WIDGET_EDIT_SIGN = progWidgetTexture("edit_sign_piece.png");
    public static final ResourceLocation PROG_WIDGET_CONDITION_REDSTONE = progWidgetTexture("condition_redstone_piece.png");
    public static final ResourceLocation PROG_WIDGET_CONDITION_ENTITY = progWidgetTexture("condition_entity_piece.png");
    public static final ResourceLocation PROG_WIDGET_CONDITION_LIQUID_INVENTORY = progWidgetTexture("condition_liquid_inventory_piece.png");
    public static final ResourceLocation PROG_WIDGET_CONDITION_ITEM_INVENTORY = progWidgetTexture("condition_item_inventory_piece.png");
    public static final ResourceLocation PROG_WIDGET_CONDITION_BLOCK = progWidgetTexture("condition_block_piece.png");
    public static final ResourceLocation PROG_WIDGET_CONDITION_ENERGY = progWidgetTexture("condition_rf_piece.png");
    public static final ResourceLocation PROG_WIDGET_CONDITION_PRESSURE = progWidgetTexture("condition_pressure_piece.png");
    public static final ResourceLocation PROG_WIDGET_CONDITION_COORDINATE = progWidgetTexture("condition_coordinate_piece.png");
    public static final ResourceLocation PROG_WIDGET_CONDITION_ITEM = progWidgetTexture("condition_item.png");
    public static final ResourceLocation PROG_WIDGET_CONDITION_LIGHT = progWidgetTexture("condition_light_piece.png");
    public static final ResourceLocation PROG_WIDGET_CONDITION_DRONE_ENTITY = progWidgetTexture("condition_drone_entity_piece.png");
    public static final ResourceLocation PROG_WIDGET_CONDITION_DRONE_LIQUID_INVENTORY = progWidgetTexture("condition_drone_liquid_piece.png");
    public static final ResourceLocation PROG_WIDGET_CONDITION_DRONE_ITEM_INVENTORY = progWidgetTexture("condition_drone_inventory_piece.png");
    public static final ResourceLocation PROG_WIDGET_CONDITION_DRONE_RF = progWidgetTexture("condition_drone_rf_piece.png");
    public static final ResourceLocation PROG_WIDGET_CONDITION_DRONE_PRESSURE = progWidgetTexture("condition_drone_pressure_piece.png");
    public static final ResourceLocation PROG_WIDGET_CONDITION_DRONE_UPGRADES = progWidgetTexture("condition_drone_upgrades_piece.png");
    public static final ResourceLocation PROG_WIDGET_VOID_ITEM = progWidgetTexture("void_item_piece.png");
    public static final ResourceLocation PROG_WIDGET_VOID_LIQUID = progWidgetTexture("void_fluid_piece.png");

    // GUI background textures
    public static final ResourceLocation GUI_BLANK_256 = guiTexture("blank256.png");
    public static final ResourceLocation GUI_AIR_COMPRESSOR = guiTexture("gui_air_compressor.png");
    public static final ResourceLocation GUI_ADVANCED_AIR_COMPRESSOR = guiTexture("gui_advanced_air_compressor.png");
    public static final ResourceLocation GUI_SOLAR_COMPRESSOR_INACTIVE = guiTexture("gui_solar_compressor_inactive.png");
    public static final ResourceLocation GUI_SOLAR_COMPRESSOR_ACTIVE = guiTexture("gui_solar_compressor_active.png");
    public static final ResourceLocation GUI_SOLAR_COMPRESSOR_BROKEN = guiTexture("gui_solar_compressor_broken.png");
    public static final ResourceLocation GUI_AIR_CANNON = guiTexture("gui_air_cannon.png");
    public static final ResourceLocation GUI_4UPGRADE_SLOTS = guiTexture("gui_pressure_chamber.png");
    public static final ResourceLocation GUI_CHARGING_STATION = guiTexture("gui_charging_station.png");
    public static final ResourceLocation GUI_CHARGING_UPGRADE_MANAGER = guiTexture("gui_charging_upgrade_manager.png");
    public static final ResourceLocation GUI_PRESSURE_CHAMBER_INTERFACE = guiTexture("gui_pressure_chamber_interface.png");
    public static final ResourceLocation GUI_VACUUM_PUMP = guiTexture("gui_vacuum_pump.png");
    public static final ResourceLocation GUI_ITEM_SEARCHER = guiTexture("gui_item_searcher.png");
    public static final ResourceLocation GUI_ASSEMBLY_CONTROLLER = guiTexture("gui_assembly_controller.png");
    public static final ResourceLocation GUI_UV_LIGHT_BOX = guiTexture("gui_uv_light_box.png");
    public static final ResourceLocation GUI_UV_LIGHT_BOX_ON = guiTexture("gui_uv_light_box_on.png");
    public static final ResourceLocation GUI_SECURITY_STATION = guiTexture("gui_security_station.png");
    public static final ResourceLocation GUI_HACKING = guiTexture("gui_hacking.png");
    public static final ResourceLocation GUI_UNIVERSAL_SENSOR = guiTexture("gui_universal_sensor.png");
    public static final ResourceLocation GUI_PNEUMATIC_DOOR = guiTexture("gui_pneumatic_door_base.png");
    public static final ResourceLocation GUI_BUILDCRAFT_ENERGY = guiTexture("gui_buildcraft_energy.png");
    public static final ResourceLocation GUI_OMNIDIRECTIONAL_HOPPER = guiTexture("gui_omnidirectional_hopper.png");
    public static final ResourceLocation GUI_LIQUID_COMPRESSOR = guiTexture("gui_liquid_compressor.png");
    public static final ResourceLocation GUI_ADVANCED_LIQUID_COMPRESSOR = guiTexture("gui_advanced_liquid_compressor.png");
    public static final ResourceLocation GUI_LIQUID_HOPPER = guiTexture( "gui_liquid_hopper.png");
    public static final ResourceLocation GUI_ELEVATOR = guiTexture("gui_elevator.png");
    public static final ResourceLocation GUI_REMOTE_EDITOR = guiTexture("gui_remote_editor.png");
    public static final ResourceLocation GUI_WIDGET_OPTIONS = guiTexture("gui_widget_options.png");
    public static final ResourceLocation GUI_PROGRAMMABLE_CONTROLLER = guiTexture("gui_programmable_controller.png");
    public static final ResourceLocation GUI_GAS_LIFT = guiTexture("gui_gas_lift.png");
    public static final ResourceLocation GUI_REFINERY = guiTexture("gui_refinery.png");
    public static final ResourceLocation GUI_THERMOPNEUMATIC_PROCESSING_PLANT = guiTexture("gui_thermopneumatic_processing_plant.png");
    public static final ResourceLocation GUI_LOGISTICS_REQUESTER = guiTexture("gui_logistics_requester.png");
    public static final ResourceLocation GUI_AMADRON = guiTexture("gui_amadron.png");
    public static final ResourceLocation GUI_KEROSENE_LAMP = guiTexture("gui_kerosene_lamp.png");
    public static final ResourceLocation GUI_SENTRY_TURRET = guiTexture("gui_sentry_turret.png");
    public static final ResourceLocation GUI_MINIGUN_MAGAZINE = guiTexture("gui_minigun_magazine.png");
    public static final ResourceLocation GUI_THERMAL_COMPRESSOR = guiTexture("gui_thermal_compressor.png");
    public static final ResourceLocation GUI_WIDGET_AREA = guiTexture("gui_widget_area.png");
    public static final ResourceLocation GUI_PASTEBIN = guiTexture("gui_pastebin.png");
    public static final ResourceLocation GUI_INVENTORY_SEARCHER = guiTexture("gui_inventory_searcher.png");
    public static final ResourceLocation GUI_TUBE_MODULE = guiTexture("gui_tube_module.png");
    public static final ResourceLocation GUI_MODULE_SIMPLE = guiTexture("gui_tube_module_simple.png");
    public static final ResourceLocation GUI_MICROMISSILE = guiTexture("gui_micromissile.png");
    public static final ResourceLocation GUI_AMADRON_ADD_TRADE = guiTexture("gui_amadron_add_trade.png");
    public static final ResourceLocation GUI_ETCHING_TANK = guiTexture("gui_etching_tank.png");
    public static final ResourceLocation GUI_FLUID_TANK = guiTexture("gui_fluid_tank.png");
    public static final ResourceLocation GUI_REINFORCED_CHEST = guiTexture("gui_reinforced_chest.png");
    public static final ResourceLocation GUI_SMART_CHEST = guiTexture("gui_smart_chest.png");
    public static final ResourceLocation GUI_TAG_WORKBENCH = guiTexture("gui_tag_workbench.png");
    public static final ResourceLocation GUI_FLUID_MIXER = guiTexture("gui_fluid_mixer.png");
    public static final ResourceLocation GUI_JACKHAMMER_SETUP = guiTexture("gui_jackhammer_setup.png");
    public static final ResourceLocation GUI_VACUUM_TRAP = guiTexture("gui_vacuum_trap.png");
    public static final ResourceLocation GUI_PRESSURIZED_SPAWNER = guiTexture("gui_pressurized_spawner.png");
    public static final ResourceLocation GUI_INSET_PANEL = guiTexture("inset_panel.png");
    public static final ResourceLocation GUI_WIDGET_TRAY = guiTexture("widget_tray.png");

    public static final ResourceLocation GUI_JEI_PRESSURE_CHAMBER = guiTexture("jei/gui_jei_pressure_chamber.png");
    public static final ResourceLocation GUI_JEI_ASSEMBLY_CONTROLLER = guiTexture("jei/gui_jei_assembly_controller.png");
    public static final ResourceLocation GUI_JEI_MISC_RECIPES = guiTexture("jei/gui_jei_misc_recipes.png");
    public static final ResourceLocation GUI_JEI_ETCHING_TANK = guiTexture("jei/gui_jei_etching_tank.png");
    public static final ResourceLocation GUI_JEI_FLUID_MIXER = guiTexture("jei/gui_jei_fluid_mixer.png");
    public static final ResourceLocation GUI_JEI_THERMOPNEUMATIC_PROCESSING_PLANT = guiTexture("jei/gui_jei_thermopneumatic_processing_plant.png");
    public static final ResourceLocation GUI_JEI_YEAST_CRAFTING = guiTexture("jei/gui_jei_yeast_crafting.png");
    public static final ResourceLocation GUI_JEI_BONUS = guiTexture("jei/gui_jei_bonus.png");
    public static final ResourceLocation GUI_JEI_SPAWNER_EXTRACTION = guiTexture("jei/gui_jei_spawner_extraction.png");
    public static final ResourceLocation GUI_JEI_HEAT_PROPERTIES = guiTexture("jei/gui_jei_heat_properties.png");
    public static final ResourceLocation GUI_JEI_MEMORY_ESSENCE = guiTexture("jei/gui_jei_memory_essence.png");
    
    // misc GUI Icons
    public static final ResourceLocation GUI_COPY_ICON_LOCATION = guiIconTexture("gui_copy.png");
    public static final ResourceLocation GUI_DELETE_ICON_LOCATION = guiIconTexture("gui_delete.png");
    public static final ResourceLocation GUI_INFO_LOCATION = guiIconTexture("gui_info.png");
    public static final ResourceLocation GUI_MOUSE_LOCATION = guiIconTexture("gui_mouse.png");
    public static final ResourceLocation GUI_NO_PROBLEMS_TEXTURE = guiIconTexture("gui_no_problem.png");
    public static final ResourceLocation GUI_PASTE_ICON_LOCATION = guiIconTexture("gui_paste.png");
    public static final ResourceLocation GUI_PASTEBIN_ICON_LOCATION = guiIconTexture("gui_pastebin_icon.png");
    public static final ResourceLocation GUI_PROBLEMS_TEXTURE = guiIconTexture("gui_problem.png");
    public static final ResourceLocation GUI_REDO_ICON_LOCATION = guiIconTexture("gui_redo.png");
    public static final ResourceLocation GUI_UNDO_ICON_LOCATION = guiIconTexture("gui_undo.png");
    public static final ResourceLocation GUI_UPGRADES_LOCATION = guiIconTexture("gui_upgrade.png");
    public static final ResourceLocation GUI_WARNING_TEXTURE = guiIconTexture("gui_warning.png");
    public static final ResourceLocation GUI_X_BUTTON = guiIconTexture("gui_x_button.png");
    public static final ResourceLocation GUI_JEI_LOGO = guiIconTexture("jei_logo.png");
    public static final ResourceLocation GUI_HIGH_SIGNAL_ANGLE = guiIconTexture("gui_high_signal_angle.png");
    public static final ResourceLocation GUI_HIGH_SIGNAL_SPACE = guiIconTexture("gui_high_signal_space.png");
    public static final ResourceLocation GUI_CHARGING = guiIconTexture("gui_charging.png");
    public static final ResourceLocation GUI_DISCHARGING = guiIconTexture("gui_discharging.png");
    public static final ResourceLocation GUI_CHARGE_IDLE = guiIconTexture("gui_charge_idle.png");
    public static final ResourceLocation GUI_WHITELIST = guiIconTexture("gui_whitelist.png");
    public static final ResourceLocation GUI_BLACKLIST = guiIconTexture("gui_blacklist.png");
    public static final ResourceLocation GUI_BAD_LOCATION = guiIconTexture("gui_bad_location.png");
    public static final ResourceLocation GUI_OK_LOCATION = guiIconTexture("gui_ok_location.png");
    public static final ResourceLocation GUI_GREEN_PROBLEMS_TEXTURE = guiIconTexture("gui_green_problem.png");
    public static final ResourceLocation GUI_SLOTS_9x3 = guiIconTexture("slots_9x3.png");
    public static final ResourceLocation GUI_SLOTS_9x1 = guiIconTexture("slots_9x1.png");

    // widget textures
    public static final ResourceLocation WIDGET_ENERGY = guiTexture("widget/widget_energy.png");
    public static final ResourceLocation WIDGET_TEMPERATURE = guiTexture("widget/widget_temperature.png");
    public static final ResourceLocation WIDGET_TANK = guiTexture("widget/widget_tank.png");
    public static final ResourceLocation WIDGET_AMADRON_OFFER = guiTexture("widget/widget_amadron_offer.png");
    public static final ResourceLocation WIDGET_VERTICAL_SCROLLBAR = guiTexture("widget/widget_vertical_scrollbar.png");
    public static final ResourceLocation JEI_EXPLOSION = guiTexture("jei/explosion.png");
    public static final ResourceLocation JEI_THERMOMETER = guiTexture("jei/thermometer.png");


    // misc rendering textures
    public static final ResourceLocation MINIGUN_CROSSHAIR = ResourceLocation.parse(RENDER_LOCATION + "minigun_crosshair.png");
    public static final ResourceLocation RENDER_LASER = ResourceLocation.parse(RENDER_LOCATION + "laser/laser.png");
    public static final ResourceLocation RENDER_LASER_OVERLAY = ResourceLocation.parse(RENDER_LOCATION + "laser/laser_overlay.png");
    public static final ResourceLocation RENDER_LASER_START = ResourceLocation.parse(RENDER_LOCATION + "laser/laser_start.png");
    public static final ResourceLocation RENDER_LASER_START_OVERLAY = ResourceLocation.parse(RENDER_LOCATION + "laser/laser_start_overlay.png");
    public static final ResourceLocation GLOW_RESOURCE = ResourceLocation.parse(RENDER_LOCATION + "blur.png");

    // entities
    public static final ResourceLocation DRONE_ENTITY = droneTexture("default_drone.png");
    public static final ResourceLocation GUARD_DRONE_ENTITY = droneTexture("guard_drone.png");
    public static final ResourceLocation HARVESTING_DRONE_ENTITY = droneTexture("harvesting_drone.png");
    public static final ResourceLocation LOGISTICS_DRONE_ENTITY = droneTexture("logistics_drone.png");
    public static final ResourceLocation COLLECTOR_DRONE_ENTITY = droneTexture("collector_drone.png");
    public static final ResourceLocation AMADRONE_ENTITY = droneTexture("amadrone.png");
    public static final ResourceLocation VORTEX_ENTITY = entityTexture("vortex.png");
    public static final ResourceLocation MICROMISSILE_ENTITY = entityTexture("micromissile.png");

    private static ResourceLocation guiTexture(String img) {
        return ResourceLocation.parse(GUI_LOCATION + img);
    }

    public static ResourceLocation guiIconTexture(String img) {
        return ResourceLocation.parse(GUI_LOCATION + "icon/" + img);
    }

    public static ResourceLocation modelTexture(String img) {
        return ResourceLocation.parse(MODEL_LOCATION + img);
    }

    private static ResourceLocation tubeModuleTexture(String img) {
        return ResourceLocation.parse(TUBE_MODULE_MODEL_LOCATION + img);
    }

    public static ResourceLocation progWidgetTexture(String img) {
        return ResourceLocation.parse(PROG_WIDGET_LOCATION + img);
    }

    public static ResourceLocation entityTexture(String img) {
        return ResourceLocation.parse(ENTITY_LOCATION + img);
    }

    public static ResourceLocation droneTexture(String img) {
        return ResourceLocation.parse(DRONE_LOCATION + img);
    }
}
