package me.desht.pneumaticcraft.common.thirdparty.cofhcore;

import com.google.common.collect.Maps;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class ThermalFoundationExplosiveLaunching {
    private static final Map<ResourceLocation, EntityType<?>> launchMap = Maps.newHashMap();

    public static void registerExplosiveLaunchBehaviour() {
        // Adds thermal TNT to launch map
        register("fire_tnt");
        register("ice_tnt");
        register("lightning_tnt");
        register("earth_tnt");
        register("ender_tnt");
        register("glowstone_tnt");
        register("redstone_tnt");
        register("slime_tnt");
        register("phyto_tnt");
        register("nuke_tnt");

        // Adds thermal grenades to launch map
        register("explosive_grenade");
        register("fire_grenade");
        register("ice_grenade");
        register("lightning_grenade");
        register("earth_grenade");
        register("ender_grenade");
        register("glowstone_grenade");
        register("redstone_grenade");
        register("slime_grenade");
        register("phyto_grenade");
        register("nuke_grenade");

        // Registers launch map
        PneumaticRegistry.getInstance().getItemRegistry().registerItemLaunchBehaviour((stack, player) -> {
            EntityType<?> entityType = launchMap.get(stack.getItem().getRegistryName());
            return entityType != null ? entityType.create(player.getLevel()) : null;
        });
    }

    /**
     * Adds the item and entity matching the passed ID to the launch map to be registered as launch behaviors
     * @param itemIDString item ID of item/entity to add to launch map
     */
    private static void register(String itemIDString) {
        ResourceLocation itemId = new ResourceLocation(ModIds.THERMAL, itemIDString);
        ResourceLocation entityId = new ResourceLocation(ModIds.THERMAL, itemIDString);
        EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(entityId);
        if (entityType != null) {
            launchMap.put(itemId, entityType);
        }
    }
}
