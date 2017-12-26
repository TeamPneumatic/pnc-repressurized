package me.desht.pneumaticcraft.common.util;

import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Gather all the reflection work we need to do here for ease of reference.
 */
public class Reflections {
    private static Method msbl_isActivated;
    private static Method msbl_getEntityId;

    public static Class blaze_aiFireballAttack;
    public static Class ghast_aiFireballAttack;

    public static void init() {
        msbl_isActivated = ReflectionHelper.findMethod(MobSpawnerBaseLogic.class, "isActivated", "func_98279_f");
        msbl_getEntityId = ReflectionHelper.findMethod(MobSpawnerBaseLogic.class, "getEntityId", "func_190895_g");
        
        // access to non-public entity AI's for hacking purposes
        blaze_aiFireballAttack = findEnclosedClass(EntityBlaze.class, "AIFireballAttack", "a");
        ghast_aiFireballAttack = findEnclosedClass(EntityGhast.class, "AIFireballAttack", "c");
    }

    private static Class<?> findEnclosedClass(Class<?> cls, String... enclosedClassNames) {
        for (Class c : cls.getDeclaredClasses()) {
            for (String name : enclosedClassNames) {
                if (c.getSimpleName().equals(name)) {
                    return c;
                }
            }
        }
        return null;
    }

    public static ResourceLocation getEntityId(MobSpawnerBaseLogic msbl) {
        try {
            return (ResourceLocation) msbl_getEntityId.invoke(msbl);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isActivated(MobSpawnerBaseLogic msbl) {
        try {
            return (boolean) msbl_isActivated.invoke(msbl);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }
}
