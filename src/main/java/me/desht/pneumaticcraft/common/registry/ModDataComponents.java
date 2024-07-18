package me.desht.pneumaticcraft.common.registry;

import com.mojang.serialization.Codec;
import me.desht.pneumaticcraft.api.item.ISpawnerCoreStats;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.amadron.ImmutableBasket;
import me.desht.pneumaticcraft.common.block.entity.utility.AphorismTileBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.utility.SmartChestBlockEntity;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetArea;
import me.desht.pneumaticcraft.common.drone.progwidgets.SavedDroneProgram;
import me.desht.pneumaticcraft.common.item.ClassifyFilterItem;
import me.desht.pneumaticcraft.common.item.JackHammerItem.DigMode;
import me.desht.pneumaticcraft.common.item.MicromissilesItem;
import me.desht.pneumaticcraft.common.item.SpawnerCoreItem;
import me.desht.pneumaticcraft.common.network.DronePacket;
import me.desht.pneumaticcraft.common.remote.SavedRemoteLayout;
import me.desht.pneumaticcraft.common.upgrades.SavedUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS
            = DeferredRegister.createDataComponents(Names.MOD_ID);

    private static <T> Supplier<DataComponentType<T>> register(String name, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return COMPONENTS.registerComponentType(name, builder -> builder
                .persistent(codec)
                .networkSynchronized(streamCodec)
        );
    }

    // Stored air in pneumatic items
    public static final Supplier<DataComponentType<Integer>> AIR
            = register("air", Codec.INT, ByteBufCodecs.INT);

    // Minigun
    public static final Supplier<DataComponentType<Integer>> OWNER_ENTITY_ID
            = register("owner_entity_id", Codec.INT, ByteBufCodecs.INT);
    public static final Supplier<DataComponentType<Integer>> MINIGUN_LOCKED_SLOT
            = register("minigun_locked_slot", ExtraCodecs.NON_NEGATIVE_INT, ByteBufCodecs.VAR_INT);
    public static final Supplier<DataComponentType<ItemContainerContents>> MINGUN_MAGAZINE
            = register("minigun_magazine", ItemContainerContents.CODEC, ItemContainerContents.STREAM_CODEC);
    public static final Supplier<DataComponentType<ItemContainerContents>> POTION_AMMO
            = register("potion_ammo", ItemContainerContents.CODEC, ItemContainerContents.STREAM_CODEC);

    // General saved inventory for block entities which need it
    public static final Supplier<DataComponentType<ItemContainerContents>> BLOCK_ENTITY_SAVED_INV
            = register("saved_inv", ItemContainerContents.CODEC, ItemContainerContents.STREAM_CODEC);
    // Extended Smart Chest saved inventory
    public static final Supplier<DataComponentType<SmartChestBlockEntity.SavedData>> SMART_CHEST_SAVED
            = register("smartchest_saved_inv", SmartChestBlockEntity.SavedData.CODEC, SmartChestBlockEntity.SavedData.STREAM_CODEC);


    // Amadron
    public static final Supplier<DataComponentType<GlobalPos>> AMADRON_ITEM_POS
            = register("amadron_item_pos", GlobalPos.CODEC, GlobalPos.STREAM_CODEC);
    public static final Supplier<DataComponentType<GlobalPos>> AMADRON_FLUID_POS
            = register("amadron_fluid_pos", GlobalPos.CODEC, GlobalPos.STREAM_CODEC);
    public static final Supplier<DataComponentType<ImmutableBasket>> AMADRON_SHOPPING_BASKET
            = register("amadron_basket", ImmutableBasket.CODEC, ImmutableBasket.STREAM_CODEC);

    // GPS Area Tool
    public static final Supplier<DataComponentType<ProgWidgetArea.Immutable>> AREA_WIDGET
            = register("area_widget", ProgWidgetArea.Immutable.CODEC, ProgWidgetArea.Immutable.STREAM_CODEC);

    // Tag Filter
    public static final Supplier<DataComponentType<List<ResourceLocation>>> TAG_FILTER_KEYS
            = register("tag_filter_keys", ResourceLocation.CODEC.listOf(), ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()));
    // Classify Filter
    public static final Supplier<DataComponentType<ClassifyFilterItem.FilterSettings>> CLASSIFY_FILTER_SETTINGS
            = register("classify_filter_settings", ClassifyFilterItem.FilterSettings.CODEC, ClassifyFilterItem.FilterSettings.STREAM_CODEC);

    // Item upgrades
    public static final Supplier<DataComponentType<SavedUpgrades>> ITEM_UPGRADES
            = register("upgrades", SavedUpgrades.CODEC, SavedUpgrades.STREAM_CODEC);

    // Ejection direction for dispenser upgrade
    public static final Supplier<DataComponentType<Direction>> EJECT_DIR
            = register("eject_dir", Direction.CODEC, Direction.STREAM_CODEC);

    // Pneumatic Helmet debug target
    public static final Supplier<DataComponentType<DronePacket.DroneTarget>> DRONE_DEBUG_TARGET
            = register("drone_debug_target", DronePacket.DroneTarget.CODEC, DronePacket.DroneTarget.STREAM_CODEC);
    // Pneumatic Armor secondary & eyepiece colors (uses vanilla DYED_COLOR component for primary)
    public static final Supplier<DataComponentType<DyedItemColor>> ARMOR_SECONDARY_COLOR
            = register("armor_secondary_color", DyedItemColor.CODEC, DyedItemColor.STREAM_CODEC);
    public static final Supplier<DataComponentType<DyedItemColor>> ARMOR_EYEPIECE_COLOR
            = register("armor_eyepiece_color", DyedItemColor.CODEC, DyedItemColor.STREAM_CODEC);
    // Pneumatic Helmet search item
    public static final Supplier<DataComponentType<Item>> HELMET_SEARCH_ITEM
            = register("helmet_search_item", BuiltInRegistries.ITEM.byNameCodec(), ByteBufCodecs.registry(Registries.ITEM));
    // Entity filter string on armor and dropped Sentry Turrets
    public static final Supplier<DataComponentType<String>> ENTITY_FILTER
            = register("entity_filter", Codec.STRING, ByteBufCodecs.STRING_UTF8);
    // Coordinate tracker target
    public static final Supplier<DataComponentType<GlobalPos>> COORD_TRACKER
            = register("coord_tracker", GlobalPos.CODEC, GlobalPos.STREAM_CODEC);
    // Current leggings speed boost percent
    public static final Supplier<DataComponentType<Integer>> SPEED_BOOST_PCT
            = register("speed_boost_pct", Codec.INT, ByteBufCodecs.VAR_INT);
    // Current leggings jump boost percent
    public static final Supplier<DataComponentType<Integer>> JUMP_BOOST_PCT
            = register("jump_boost_pct", Codec.INT, ByteBufCodecs.VAR_INT);
    // Current jet boots power percent
    public static final Supplier<DataComponentType<Integer>> JET_BOOTS_PCT
            = register("jet_boots_pct", Codec.INT, ByteBufCodecs.VAR_INT);
    // Current jet boots builder mode
    public static final Supplier<DataComponentType<Boolean>> JET_BOOTS_BUILDER_MODE
            = register("jet_boots_builder_mode", Codec.BOOL, ByteBufCodecs.BOOL);
    // Current jet boots builder mode
    public static final Supplier<DataComponentType<Boolean>> JET_BOOTS_STABILIZERS
            = register("jet_boots_stabilizers", Codec.BOOL, ByteBufCodecs.BOOL);
    // Current jet boots builder mode
    public static final Supplier<DataComponentType<Boolean>> JET_BOOTS_HOVER
            = register("jet_boots_hover", Codec.BOOL, ByteBufCodecs.BOOL);
    // Current jet boots builder mode
    public static final Supplier<DataComponentType<Boolean>> JET_BOOTS_SMART_HOVER
            = register("jet_boots_smart_hover", Codec.BOOL, ByteBufCodecs.BOOL);

    // Micromissile settings
    public static final Supplier<DataComponentType<MicromissilesItem.Settings>> MICROMISSILE_SETTINGS
            = register("micromissile_settings", MicromissilesItem.Settings.CODEC, MicromissilesItem.Settings.STREAM_CODEC);

    // Remote Layout
    public static final Supplier<DataComponentType<SavedRemoteLayout>> REMOTE_LAYOUT
            = register("remote_layout", SavedRemoteLayout.CODEC, SavedRemoteLayout.STREAM_CODEC);
    // Remote security station position
    public static final Supplier<DataComponentType<GlobalPos>> REMOTE_SECSTATION_POS
            = register("remote_secstation_pos", GlobalPos.CODEC, GlobalPos.STREAM_CODEC);

    // Aphorism Tile data
    public static final Supplier<DataComponentType<AphorismTileBlockEntity.SavedData>> APHORISM_TILE_DATA
            = register("aphorism_tile_data", AphorismTileBlockEntity.SavedData.CODEC, AphorismTileBlockEntity.SavedData.STREAM_CODEC);

    // Memory Stick orb absorption
    public static final Supplier<DataComponentType<Boolean>> ABSORB_ORBS
            = register("absorb_orbs", Codec.BOOL, ByteBufCodecs.BOOL);

    // Drone saved progwidgets
    public static final Supplier<DataComponentType<SavedDroneProgram>> SAVED_DRONE_PROGRAM
            = register("saved_drone_program", SavedDroneProgram.CODEC, SavedDroneProgram.STREAM_CODEC);
    // Drone color
    public static final Supplier<DataComponentType<Integer>> DRONE_COLOR
            = register("drone_color", Codec.INT, ByteBufCodecs.INT);

    // General stored fluid component - used by Drones and Memory Sticks
    public static final Supplier<DataComponentType<SimpleFluidContent>> STORED_FLUID
            = register("stored_fluid", SimpleFluidContent.CODEC, SimpleFluidContent.STREAM_CODEC);

    // UV exposure for PCB's
    public static final Supplier<DataComponentType<Integer>> UV_EXPOSURE
            = register("uv_exposure", Codec.INT, ByteBufCodecs.INT);

    // Block entity tank(s), serialized
    public static final Supplier<DataComponentType<SimpleFluidContent>> MAIN_TANK
            = register("main_tank", SimpleFluidContent.CODEC, SimpleFluidContent.STREAM_CODEC);
    public static final Supplier<DataComponentType<SimpleFluidContent>> INPUT_TANK_1
            = register("input_tank_1", SimpleFluidContent.CODEC, SimpleFluidContent.STREAM_CODEC);
    public static final Supplier<DataComponentType<SimpleFluidContent>> INPUT_TANK_2
            = register("input_tank_2", SimpleFluidContent.CODEC, SimpleFluidContent.STREAM_CODEC);
    public static final Supplier<DataComponentType<SimpleFluidContent>> OUTPUT_TANK
            = register("output_tank", SimpleFluidContent.CODEC, SimpleFluidContent.STREAM_CODEC);

    public static final List<Supplier<DataComponentType<SimpleFluidContent>>> TANK_COMPONENTS = List.of(
            MAIN_TANK, INPUT_TANK_1, INPUT_TANK_2, OUTPUT_TANK
    );

    // General block entity saved side config & redstone settings
    public static final Supplier<DataComponentType<CustomData>> SAVED_SIDE_CONFIG
            = register("be_saved_sideconfig", CustomData.CODEC, CustomData.STREAM_CODEC);
    public static final Supplier<DataComponentType<CustomData>> SAVED_REDSTONE_CONTROLLER
            = register("be_saved_rc", CustomData.CODEC, CustomData.STREAM_CODEC);

    // Solar compressor broken
    public static final Supplier<DataComponentType<Boolean>> SOLAR_BROKEN
            = register("solar_broken", Codec.BOOL, ByteBufCodecs.BOOL);

    // Charging Station upgrade-only mode
    public static final Supplier<DataComponentType<Boolean>> UPGRADE_ONLY
            = register("upgrade_only", Codec.BOOL, ByteBufCodecs.BOOL);

    // Pneumatic Door color
    public static final Supplier<DataComponentType<DyeColor>> DOOR_COLOR
            = register("door_color", DyeColor.CODEC, DyeColor.STREAM_CODEC);

    // Camouflage Applicator blockstate
    public static final Supplier<DataComponentType<BlockState>> CAMO_STATE
            = register("camo_state", BlockState.CODEC, ByteBufCodecs.fromCodec(BlockState.CODEC));

    // PCB Etch progress
    public static final Supplier<DataComponentType<Integer>> ETCH_PROGRESS
            = register("etch_progress", Codec.INT, ByteBufCodecs.INT);

    // GPS Tool blockpos & variable
    public static final Supplier<DataComponentType<BlockPos>> GPS_TOOL_POS
            = register("gps_tool_pos", BlockPos.CODEC, BlockPos.STREAM_CODEC);
    public static final Supplier<DataComponentType<String>> GPS_TOOL_VAR
            = register("gps_tool_var", Codec.STRING, ByteBufCodecs.STRING_UTF8);

    // Spawner Core stats
    public static final Supplier<DataComponentType<ISpawnerCoreStats>> SPAWNER_CORE_STATS
            = register("spawner_core_stats", SpawnerCoreItem.SpawnerCoreStats.CODEC, SpawnerCoreItem.SpawnerCoreStats.STREAM_CODEC);

    // Saved semiblock data
    public static final Supplier<DataComponentType<CustomData>> SEMIBLOCK_DATA
            = register("semiblock_data", CustomData.CODEC, CustomData.STREAM_CODEC);

    // JackHammer dig mode
    public static final Supplier<DataComponentType<DigMode>> JACKHAMMER_DIG_MODE
            = register("jackhammer_dig_mode", StringRepresentable.fromEnum(DigMode::values), NeoForgeStreamCodecs.enumCodec(DigMode.class));
    // JackHammer stored drill bit
    public static final Supplier<DataComponentType<ItemContainerContents>> JACKHAMMER_DRILL_BIT
            = register("jackhammer_drill_bit", ItemContainerContents.CODEC, ItemContainerContents.STREAM_CODEC);
}
