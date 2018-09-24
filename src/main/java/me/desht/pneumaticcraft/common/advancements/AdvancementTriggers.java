package me.desht.pneumaticcraft.common.advancements;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AdvancementTriggers {
    public static final CustomTrigger EXPLODE_IRON = new CustomTrigger("root");
    public static final CustomTrigger OIL_BUCKET = new CustomTrigger("oil_bucket");
    public static final CustomTrigger NINEBYNINE = new CustomTrigger("9x9");
    public static final CustomTrigger PRESSURE_CHAMBER = new CustomTrigger("pressure_chamber");
    public static final CustomTrigger PROGRAM_DRONE = new CustomTrigger("program_drone");
    public static final CustomTrigger PNEUMATIC_ARMOR = new CustomTrigger("pneumatic_armor");
    public static final CustomTrigger ENTITY_HACK = new CustomTrigger("entity_hack");
    public static final CustomTrigger BLOCK_HACK = new CustomTrigger("block_hack");
    public static final CustomTrigger FLIGHT = new CustomTrigger("flight");
    public static final CustomTrigger FLY_INTO_WALL = new CustomTrigger("fly_into_wall");
    public static final CustomTrigger LOGISTICS_DRONE_DEPLOYED = new CustomTrigger("logistics_drone_deployed");

    private static final CustomTrigger[] ALL_TRIGGERS = new CustomTrigger[] {
            EXPLODE_IRON,
            OIL_BUCKET,
            NINEBYNINE,
            PRESSURE_CHAMBER,
            PROGRAM_DRONE,
            PNEUMATIC_ARMOR,
            ENTITY_HACK,
            BLOCK_HACK,
            FLIGHT,
            FLY_INTO_WALL,
            LOGISTICS_DRONE_DEPLOYED
    };

    public static void registerTriggers() {
        Method method;
        try {
            method = ReflectionHelper.findMethod(CriteriaTriggers.class, "register", "func_192118_a", ICriterionTrigger.class);
            method.setAccessible(true);
            for (CustomTrigger trigger : ALL_TRIGGERS) {
                method.invoke(null, trigger);
            }
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
