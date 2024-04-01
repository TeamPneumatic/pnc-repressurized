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
import net.minecraft.world.entity.animal.Pufferfish;

/**
 * Gather all the reflection work we need to do here for ease of reference.
 *
 * Note: any private field access is handled via access transformers (META-INF/pneumaticcraft_at.cfg)
 */
public class Reflections {
//    public static Class<?> blaze_aiFireballAttack;
//    public static Class<?> ghast_aiFireballAttack;
//    public static Class<?> shulker_aiAttack;
//    public static Class<?> guardian_aiGuardianAttack;
    public static Class<?> pufferfish_aiPuff;

    public static void init() {
        // access to non-public entity AI's for hacking purposes
        // TODO 1.14 verify notch names
//        blaze_aiFireballAttack = findEnclosedClass(Blaze.class, "BlazeAttackGoal", "a");
//        ghast_aiFireballAttack = findEnclosedClass(Ghast.class, "GhastShootFireballGoal", "c");
//        shulker_aiAttack = findEnclosedClass(Shulker.class, "ShulkerAttackGoal", "a");
//        guardian_aiGuardianAttack = findEnclosedClass(Guardian.class, "GuardianAttackGoal", "a");
        pufferfish_aiPuff = findEnclosedClass(Pufferfish.class, "PufferfishPuffGoal", "a");
    }

    private static Class<?> findEnclosedClass(Class<?> cls, String... enclosedClassNames) {
        for (Class<?> c : cls.getDeclaredClasses()) {
            for (String name : enclosedClassNames) {
                if (c.getSimpleName().equals(name)) {
                    return c;
                }
            }
        }
        Log.error("can't find any of [{}] in class {}", Strings.join(enclosedClassNames, ", "),cls.getCanonicalName());
        return null;
    }
}
