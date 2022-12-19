package me.desht.pneumaticcraft.common.thirdparty.botania;

import com.google.common.collect.Maps;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class MinecartLaunching {
    private static final Map<ResourceLocation, EntityType<?>> launchMap = Maps.newHashMap();

    public static void registerMinecartLaunchBehaviour() {
        // Adds botania minecarts to launch map
        register("pool_minecart");

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
        ResourceLocation itemId = new ResourceLocation(ModIds.BOTANIA, itemIDString);
        ResourceLocation entityId = new ResourceLocation(ModIds.BOTANIA, itemIDString);
        EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(entityId);
        if (entityType != null) {
            launchMap.put(itemId, entityType);
        }
    }
}