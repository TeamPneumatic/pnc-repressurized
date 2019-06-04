package me.desht.pneumaticcraft.common.util;

import joptsimple.internal.Strings;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Gather all the reflection work we need to do here for ease of reference.
 *
 * Note: any private field access is handled via access transformers (META-INF/pneumaticcraft_at.cfg)
 */
public class Reflections {
    private static Method msbl_isActivated;

    public static Class blaze_aiFireballAttack;
    public static Class ghast_aiFireballAttack;
    public static Class shulker_aiAttack;
    public static Class guardian_aiGuardianAttack;

    public static void init() {
        msbl_isActivated = ReflectionHelper.findMethod(MobSpawnerBaseLogic.class, "isActivated", "func_98279_f");

        // access to non-public entity AI's for hacking purposes
        blaze_aiFireballAttack = findEnclosedClass(EntityBlaze.class, "AIFireballAttack", "a");
        ghast_aiFireballAttack = findEnclosedClass(EntityGhast.class, "AIFireballAttack", "c");
        shulker_aiAttack = findEnclosedClass(EntityShulker.class, "AIAttack", "a");
        guardian_aiGuardianAttack = findEnclosedClass(EntityGuardian.class, "AIGuardianAttack", "a");
    }

    private static Class<?> findEnclosedClass(Class<?> cls, String... enclosedClassNames) {
        for (Class c : cls.getDeclaredClasses()) {
            for (String name : enclosedClassNames) {
                if (c.getSimpleName().equals(name)) {
                    return c;
                }
            }
        }
        Log.error("can't find any of [" + Strings.join(enclosedClassNames, ", ") + "] in class " + cls.getCanonicalName());
        return null;
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
