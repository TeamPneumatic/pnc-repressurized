package me.desht.pneumaticcraft.common.thirdparty.cofhcore;

import com.google.common.collect.Maps;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.Map;

public class ThermalLocomotionMinecartLaunching {
    private static final Map<ResourceLocation, EntityType<?>> launchMap = Maps.newHashMap();

    public static void registerMinecartLaunchBehaviour() {
        // Adds thermal minecarts to launch map
        register("underwater_minecart");
        register("fire_tnt_minecart");
        register("ice_tnt_minecart");
        register("lightning_tnt_minecart");
        register("earth_tnt_minecart");
        register("ender_tnt_minecart");
        register("glowstone_tnt_minecart");
        register("redstone_tnt_minecart");
        register("slime_tnt_minecart");
        register("phyto_tnt_minecart");
        register("nuke_tnt_minecart");

        // Registers launch map
        PneumaticRegistry.getInstance().getItemRegistry().registerItemLaunchBehaviour((stack, player) -> {
            EntityType<?> entityType = launchMap.get(PneumaticCraftUtils.getRegistryName(stack.getItem()).orElseThrow());
            return entityType != null ? entityType.create(player.level()) : null;
        });
    }

    /**
     * Adds the item and entity matching the passed ID to the launch map to be registered as launch behaviors
     * @param itemIDString item ID of item/entity to add to launch map
     */
    private static void register(String itemIDString) {
        ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(ModIds.THERMAL, itemIDString);
        ResourceLocation entityId = ResourceLocation.fromNamespaceAndPath(ModIds.THERMAL, itemIDString);
        BuiltInRegistries.ENTITY_TYPE.getOptional(entityId).ifPresent(entityType -> launchMap.put(itemId, entityType));
    }
}