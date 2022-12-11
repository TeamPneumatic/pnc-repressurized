package me.desht.pneumaticcraft.common.thirdparty.cofhcore;

import com.google.common.collect.Maps;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class ThermalExplosiveLaunching {
    private static final Map<ResourceLocation, EntityType<?>> launchMap = Maps.newHashMap();

    public static void registerExplosiveLaunchBehaviour() {
        register("ice");
        register("earth");
        register("lightning");

        PneumaticRegistry.getInstance().getItemRegistry().registerItemLaunchBehaviour((stack, player) -> {
            EntityType<?> entityType = launchMap.get(stack.getItem().getRegistryName());
            return entityType != null ? entityType.create(player.getLevel()) : null;
        });
    }

    public static void registerMinecartLaunchBehaviour() {
        register("ice");
        register("earth");
        register("lightning");

        PneumaticRegistry.getInstance().getItemRegistry().registerItemLaunchBehaviour((stack, player) -> {
            EntityType<?> entityType = launchMap.get(stack.getItem().getRegistryName());
            return entityType != null ? entityType.create(player.getLevel()) : null;
        });
    }

    private static void register(String idStr) {
        ResourceLocation itemId = new ResourceLocation(ModIds.THERMAL, idStr + "_charge");
        ResourceLocation entityId = new ResourceLocation(ModIds.THERMAL, idStr + "_grenade");
        EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(entityId);
        if (entityType != null) {
            launchMap.put(itemId, entityType);
        }
    }
}
