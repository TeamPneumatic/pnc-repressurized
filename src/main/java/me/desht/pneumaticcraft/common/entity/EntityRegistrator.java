package me.desht.pneumaticcraft.common.entity;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityHarvestingDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityLogisticsDrone;
import me.desht.pneumaticcraft.common.entity.projectile.EntityVortex;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class EntityRegistrator {
    public static void init() {
        EntityRegistry.registerModEntity(RL("vortex"), EntityVortex.class, "Vortex", 0, PneumaticCraftRepressurized.instance, 80, 1, true);
        EntityRegistry.registerModEntity(RL("drone"), EntityDrone.class, "Drone", 1, PneumaticCraftRepressurized.instance, 80, 1, true);
        EntityRegistry.registerModEntity(RL("logistic_drone"), EntityLogisticsDrone.class, "logisticDrone", 2, PneumaticCraftRepressurized.instance, 80, 1, true);
        EntityRegistry.registerModEntity(RL("harvesting_drone"), EntityHarvestingDrone.class, "harvestingDrone", 3, PneumaticCraftRepressurized.instance, 80, 1, true);
        EntityRegistry.registerModEntity(RL("ring"), EntityRing.class, Names.MOD_ID + ".ring", 100, PneumaticCraftRepressurized.instance, 80, 1, true);
    }
}
