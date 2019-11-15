package me.desht.pneumaticcraft.common.heat.behaviour;

import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeatBehaviourManager {
    private static final HeatBehaviourManager INSTANCE = new HeatBehaviourManager();
    private final Map<ResourceLocation, HeatBehaviour> behaviourRegistry = new HashMap<>();

    public static HeatBehaviourManager getInstance() {
        return INSTANCE;
    }

    public void onPostInit() {
        registerBehaviour(HeatBehaviourFurnace.class);
        registerBehaviour(HeatBehaviourHeatFrame.class);

        // this handles any custom non-tile-entity blocks and fluids, vanilla and modded
        registerBehaviour(HeatBehaviourCustomTransition.class);
    }

    public void registerBehaviour(Class<? extends HeatBehaviour> behaviour) {
        if (behaviour == null) throw new IllegalArgumentException("Can't register a null behaviour!");
        try {
            HeatBehaviour instance = behaviour.newInstance();
            HeatBehaviour prevInstance = behaviourRegistry.put(instance.getId(), instance);
            if (prevInstance != null)
                Log.warning("Registered a heat behaviour that has the same id as an already registered one. The old one will be discarded. Old behaviour class: " + prevInstance.getClass().getName() + ". New class: " + behaviour.getName());
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("The behaviour class doesn't have a nullary constructor, or is abstract! Class: " + behaviour);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Doesn't have access to the class (is it private?) Class: " + behaviour);
        }
    }

    public HeatBehaviour makeNewBehaviourForId(ResourceLocation id) {
        HeatBehaviour behaviour = behaviourRegistry.get(id);
        if (behaviour != null) {
            try {
                return behaviour.getClass().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            Log.warning("No heat behaviour found for id: " + id);
            return null;
        }
    }

    public void addHeatBehaviours(World world, BlockPos pos, Direction direction, IHeatExchangerLogic logic, List<HeatBehaviour> list) {
        for (HeatBehaviour behaviour : behaviourRegistry.values()) {
            behaviour.initialize(logic, world, pos, direction);
            if (behaviour.isApplicable()) {
                try {
                    behaviour = behaviour.getClass().newInstance();
                    behaviour.initialize(logic, world, pos, direction);
                    list.add(behaviour);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
