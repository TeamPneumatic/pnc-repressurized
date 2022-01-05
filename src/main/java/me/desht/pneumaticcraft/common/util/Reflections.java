/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.util;

import joptsimple.internal.Strings;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.entity.monster.GhastEntity;
import net.minecraft.entity.monster.GuardianEntity;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Gather all the reflection work we need to do here for ease of reference.
 *
 * Note: any private field access is handled via access transformers (META-INF/pneumaticcraft_at.cfg)
 */
public class Reflections {
    private static Method msbl_isActivated;

    public static Class<?> blaze_aiFireballAttack;
    public static Class<?> ghast_aiFireballAttack;
    public static Class<?> shulker_aiAttack;
    public static Class<?> guardian_aiGuardianAttack;

    public static void init() {
        msbl_isActivated = ObfuscationReflectionHelper.findMethod(AbstractSpawner.class, "func_98279_f");

        // access to non-public entity AI's for hacking purposes
        // TODO 1.14 verify notch names
        blaze_aiFireballAttack = findEnclosedClass(BlazeEntity.class, "FireballAttackGoal", "a");
        ghast_aiFireballAttack = findEnclosedClass(GhastEntity.class, "FireballAttackGoal", "c");
        shulker_aiAttack = findEnclosedClass(ShulkerEntity.class, "AttackGoal", "a");
        guardian_aiGuardianAttack = findEnclosedClass(GuardianEntity.class, "AttackGoal", "a");
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

    public static boolean isActivated(AbstractSpawner msbl) {
        try {
            return (boolean) msbl_isActivated.invoke(msbl);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }
}
