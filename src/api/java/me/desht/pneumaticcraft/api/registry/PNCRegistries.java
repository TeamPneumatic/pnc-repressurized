package me.desht.pneumaticcraft.api.registry;

import me.desht.pneumaticcraft.api.drone.area.AreaType;
import me.desht.pneumaticcraft.api.drone.area.AreaTypeSerializer;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.harvesting.HarvestHandler;
import me.desht.pneumaticcraft.api.harvesting.HoeHandler;
import me.desht.pneumaticcraft.api.misc.IPlayerMatcher;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class PNCRegistries {
    // Keys
    public static final ResourceKey<Registry<AreaTypeSerializer<?>>> AREA_TYPE_SERIALIZER_KEY
            = ResourceKey.createRegistryKey(RL("prog_widget_area_serializer"));
    public static final ResourceKey<Registry<HarvestHandler>> HARVEST_HANDLERS_KEY
            = ResourceKey.createRegistryKey(RL("harvest_handlers"));
    public static final ResourceKey<Registry<HoeHandler>> HOE_HANDLERS_KEY
            = ResourceKey.createRegistryKey(RL("hoe_handlers"));
    public static final ResourceKey<Registry<IPlayerMatcher.MatcherType<?>>> PLAYER_MATCHER_KEY
            = ResourceKey.createRegistryKey(RL("player_matchers"));
    public static final ResourceKey<Registry<ProgWidgetType<?>>> PROG_WIDGETS_KEY
            = ResourceKey.createRegistryKey(RL("prog_widgets"));

    // Registries
    public static final Registry<AreaTypeSerializer<? extends AreaType>> AREA_TYPE_SERIALIZER_REGISTRY
            = new RegistryBuilder<>(AREA_TYPE_SERIALIZER_KEY).create();
    public static final Registry<HarvestHandler> HARVEST_HANDLER_REGISTRY
            = new RegistryBuilder<>(HARVEST_HANDLERS_KEY).create();
    public static final Registry<HoeHandler> HOE_HANDLER_REGISTRY
            = new RegistryBuilder<>(HOE_HANDLERS_KEY).create();
    public static final Registry<IPlayerMatcher.MatcherType<?>> PLAYER_MATCHER_REGISTRY
            = new RegistryBuilder<>(PLAYER_MATCHER_KEY).create();
    public static final Registry<ProgWidgetType<?>> PROG_WIDGETS_REGISTRY
            = new RegistryBuilder<>(PROG_WIDGETS_KEY).create();
}
