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

    private static final CustomTrigger[] ALL_TRIGGERS = new CustomTrigger[] {
            EXPLODE_IRON,
            OIL_BUCKET,
            NINEBYNINE
    };

    public static void registerTriggers() {
        Method method;
        try {
            method = ReflectionHelper.findMethod(CriteriaTriggers.class, "register", "func_192118_a", ICriterionTrigger.class);
            method.setAccessible(true);
            for (int i = 0; i < ALL_TRIGGERS.length; i++) {
                method.invoke(null, ALL_TRIGGERS[i]);
            }
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
