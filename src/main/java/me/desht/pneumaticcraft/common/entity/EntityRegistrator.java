package me.desht.pneumaticcraft.common.entity;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityLogisticsDrone;
import me.desht.pneumaticcraft.common.entity.projectile.EntityVortex;
import net.minecraft.entity.EntityList;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class EntityRegistrator {
    public static void init() {
        // Entities
        // parms: entity class, mobname (for spawners), id, modclass, max player
        // distance for update, update frequency, boolean keep server updated
        // about velocities.
        EntityRegistry.registerModEntity(RL("vortex"), EntityVortex.class, "Vortex", 0, PneumaticCraftRepressurized.instance, 80, 1, true);
        EntityRegistry.registerModEntity(RL("drone"), EntityDrone.class, "Drone", 1, PneumaticCraftRepressurized.instance, 80, 1, true);
        EntityRegistry.registerModEntity(RL("logistic_drone"), EntityLogisticsDrone.class, "logisticDrone", 2, PneumaticCraftRepressurized.instance, 80, 1, true);
        // Entity Eggs:
        // registerEntityEgg(EntityRook.class, 0xffffff, 0x000000);
    }

    private static int getUniqueEntityId() {
        int startEntityId = 0;
        do {
            startEntityId++;
        } while (EntityList.getClassFromID(startEntityId) != null);

        return startEntityId;
    }

//    public static void registerEntityEgg(Class<? extends Entity> entity, int primaryColor, int secondaryColor) {
//        int id = getUniqueEntityId();
//        EntityList.idToClassMapping.put(id, entity);
//        EntityList.entityEggs.put(id, new EntityEggInfo(id, primaryColor, secondaryColor));
//    }
}
