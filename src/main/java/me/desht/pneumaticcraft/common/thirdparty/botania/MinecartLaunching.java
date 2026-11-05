package me.desht.pneumaticcraft.common.thirdparty.botania;

import com.google.common.collect.Maps;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.Map;

public class MinecartLaunching {
    private static final Map<Identifier, EntityType<?>> launchMap = Maps.newHashMap();

    public static void registerMinecartLaunchBehaviour() {
        // Adds botania minecarts to launch map
        register("pool_minecart");

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
        Identifier itemId = Identifier.fromNamespaceAndPath(ModIds.BOTANIA, itemIDString);
        Identifier entityId = Identifier.fromNamespaceAndPath(ModIds.BOTANIA, itemIDString);
        BuiltInRegistries.ENTITY_TYPE.getOptional(entityId).ifPresent(entityType -> launchMap.put(itemId, entityType));
    }
}