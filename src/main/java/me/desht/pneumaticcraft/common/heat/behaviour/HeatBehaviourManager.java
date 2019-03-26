package me.desht.pneumaticcraft.common.heat.behaviour;

import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeatBehaviourManager {
    private static final HeatBehaviourManager INSTANCE = new HeatBehaviourManager();
    private final Map<String, HeatBehaviour> behaviours = new HashMap<>();

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
            HeatBehaviour ins = behaviour.newInstance();
            HeatBehaviour old = behaviours.put(ins.getId(), ins);
            if (old != null)
                Log.warning("Registered a heat behaviour that has the same id as an already registered one. The old one will be discarded. Old behaviour class: " + old.getClass() + ". New class: " + behaviour.getClass());
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("The behaviour class doesn't have a nullary constructor, or is abstract! Class: " + behaviour);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Doesn't have access to the class (is it private?) Class: " + behaviour);
        }
    }

    HeatBehaviour getBehaviour(String id) {
        return behaviours.get(id);
    }

    public HeatBehaviour getNewBehaviourForId(String id) {
        HeatBehaviour behaviour = behaviours.get(id);
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

    public void addHeatBehaviours(World world, BlockPos pos, EnumFacing direction, IHeatExchangerLogic logic, List<HeatBehaviour> list) {
        for (HeatBehaviour behaviour : behaviours.values()) {
            String id = behaviour.getId();
            behaviour.initialize(id, logic, world, pos, direction);
            if (behaviour.isApplicable()) {
                try {
                    behaviour = behaviour.getClass().newInstance();
                    behaviour.initialize(id, logic, world, pos, direction);
                    list.add(behaviour);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
