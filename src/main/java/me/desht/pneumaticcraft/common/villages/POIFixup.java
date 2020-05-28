package me.desht.pneumaticcraft.common.villages;

import me.desht.pneumaticcraft.common.core.ModVillagers;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This appears to be necessary to get the blockstate->poi_type mapping correct in PointOfInterestType
 */
public class POIFixup {
    private static final Method blockStatesInjector = ObfuscationReflectionHelper.findMethod(PointOfInterestType.class, "func_221052_a", PointOfInterestType.class);

    public static void fixup() {
        try {
            blockStatesInjector.invoke(null, ModVillagers.MECHANIC_POI.get());
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
