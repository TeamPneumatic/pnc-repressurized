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

package me.desht.pneumaticcraft.common.network;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.network.SyncedField.*;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class NetworkUtils {
    /**
     * Get a list of all the synced fields for a syncable object
     *
     * @param syncable the object whose fields we are extracting
     * @param searchedAnnotation the annotation type to search for
     * @return a list of all the fields annotated with the given type
     */
    public static List<SyncedField<?>> getSyncedFields(Object syncable, Class<? extends Annotation> searchedAnnotation) {
        ImmutableList.Builder<SyncedField<?>> builder = ImmutableList.builder();
        Class<?> examinedClass = syncable.getClass();
        while (examinedClass != null) {
            for (Field field : examinedClass.getDeclaredFields()) {
                if (field.getAnnotation(searchedAnnotation) != null) {
                    builder.addAll(getSyncedFieldsForField(field, syncable, searchedAnnotation));
                }
            }
            examinedClass = examinedClass.getSuperclass();
        }
        return builder.build();
    }

    private static List<SyncedField<?>> getSyncedFieldsForField(Field field, Object te, Class<? extends Annotation> searchedAnnotation) {
        boolean isLazy = field.getAnnotation(LazySynced.class) != null;
        SyncedField<?> syncedField = getSyncedFieldForField(field, te);
        if (syncedField != null) {
            return Collections.singletonList(syncedField.setLazy(isLazy));
        } else {
            ImmutableList.Builder<SyncedField<?>> builder = ImmutableList.builder();
            int filteredIndex = field.getAnnotation(FilteredSynced.class) != null ? field.getAnnotation(FilteredSynced.class).index() : -1;
            try {
                field.setAccessible(true);
                Object o = field.get(te);
                if (o instanceof int[] array) {
                    if (filteredIndex >= 0) {
                        builder.add(new SyncedInt(te, field).setArrayIndex(filteredIndex).setLazy(isLazy));
                    } else {
                        for (int i = 0; i < array.length; i++) {
                            builder.add(new SyncedInt(te, field).setArrayIndex(i).setLazy(isLazy));
                        }
                    }
                } else if (o instanceof float[] array) {
                    if (filteredIndex >= 0) {
                        builder.add(new SyncedFloat(te, field).setArrayIndex(filteredIndex).setLazy(isLazy));
                    } else {
                        for (int i = 0; i < array.length; i++) {
                            builder.add(new SyncedFloat(te, field).setArrayIndex(i).setLazy(isLazy));
                        }
                    }
                } else if (o instanceof double[] array) {
                    if (filteredIndex >= 0) {
                        builder.add(new SyncedDouble(te, field).setArrayIndex(filteredIndex).setLazy(isLazy));
                    } else {
                        for (int i = 0; i < array.length; i++) {
                            builder.add(new SyncedDouble(te, field).setArrayIndex(i).setLazy(isLazy));
                        }
                    }
                } else if (o instanceof boolean[] array) {
                    if (filteredIndex >= 0) {
                        builder.add(new SyncedBoolean(te, field).setArrayIndex(filteredIndex).setLazy(isLazy));
                    } else {
                        for (int i = 0; i < array.length; i++) {
                            builder.add(new SyncedBoolean(te, field).setArrayIndex(i).setLazy(isLazy));
                        }
                    }
                } else if (o instanceof String[] array) {
                    if (filteredIndex >= 0) {
                        builder.add(new SyncedString(te, field).setArrayIndex(filteredIndex).setLazy(isLazy));
                    } else {
                        for (int i = 0; i < array.length; i++) {
                            builder.add(new SyncedString(te, field).setArrayIndex(i).setLazy(isLazy));
                        }
                    }
                } else if (o.getClass().isArray() && o.getClass().getComponentType().isEnum()) {
                    Object[] enumArray = (Object[]) o;
                    if (filteredIndex >= 0) {
                        builder.add(new SyncedEnum(te, field).setArrayIndex(filteredIndex).setLazy(isLazy));
                    } else {
                        for (int i = 0; i < enumArray.length; i++) {
                            builder.add(new SyncedEnum(te, field).setArrayIndex(i).setLazy(isLazy));
                        }
                    }
                } else if (o instanceof ItemStack[] array) {
                    if (filteredIndex >= 0) {
                        builder.add(new SyncedItemStack(te, field).setArrayIndex(filteredIndex).setLazy(isLazy));
                    } else {
                        for (int i = 0; i < array.length; i++) {
                            builder.add(new SyncedItemStack(te, field).setArrayIndex(i).setLazy(isLazy));
                        }
                    }
                } else if (o instanceof FluidStack[] array) {
                    if (filteredIndex >= 0) {
                        builder.add(new SyncedFluidStack(te, field).setArrayIndex(filteredIndex).setLazy(isLazy));
                    } else {
                        for (int i = 0; i < array.length; i++) {
                            builder.add(new SyncedFluidStack(te, field).setArrayIndex(i).setLazy(isLazy));
                        }
                    }
                } else if (o instanceof ItemStackHandler[] array) {
                    if (filteredIndex >= 0) {
                        builder.add(new SyncedItemStack(te, field).setArrayIndex(filteredIndex).setLazy(isLazy));
                    } else {
                        for (int i = 0; i < array.length; i++) {
                            builder.add(new SyncedItemStack(te, field).setArrayIndex(i).setLazy(isLazy));
                        }
                    }
                } else if (field.getType().isArray()) {
                    Object[] array = (Object[]) o;
                    for (Object obj : array) {
                        builder.addAll(getSyncedFields(obj, searchedAnnotation));
                    }
                } else {
                    builder.addAll(getSyncedFields(o, searchedAnnotation));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            List<SyncedField<?>> syncedFields = builder.build();
            if (syncedFields.isEmpty()) Log.warning("Field " + field + " didn't produce any syncable fields!");
            return syncedFields;
        }
    }

    private static SyncedField<?> getSyncedFieldForField(Field field, Object te) {
        if (int.class.isAssignableFrom(field.getType())) return new SyncedInt(te, field);
        if (float.class.isAssignableFrom(field.getType())) return new SyncedFloat(te, field);
        if (double.class.isAssignableFrom(field.getType())) return new SyncedDouble(te, field);
        if (boolean.class.isAssignableFrom(field.getType())) return new SyncedBoolean(te, field);
        if (String.class.isAssignableFrom(field.getType())) return new SyncedString(te, field);
        if (field.getType().isEnum()) return new SyncedEnum(te, field);
        if (ItemStack.class.isAssignableFrom(field.getType())) return new SyncedItemStack(te, field);
        if (FluidStack.class.isAssignableFrom(field.getType())) return new SyncedFluidStack(te, field);
        if (IItemHandlerModifiable.class.isAssignableFrom(field.getType())) return new SyncedItemHandler(te, field);
        return null;
    }
}
