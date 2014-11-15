package pneumaticCraft.common.network;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidTank;
import pneumaticCraft.common.inventory.SyncedField;
import pneumaticCraft.common.inventory.SyncedField.SyncedBoolean;
import pneumaticCraft.common.inventory.SyncedField.SyncedDouble;
import pneumaticCraft.common.inventory.SyncedField.SyncedEnum;
import pneumaticCraft.common.inventory.SyncedField.SyncedFloat;
import pneumaticCraft.common.inventory.SyncedField.SyncedFluidTank;
import pneumaticCraft.common.inventory.SyncedField.SyncedInt;
import pneumaticCraft.common.inventory.SyncedField.SyncedItemStack;
import pneumaticCraft.common.inventory.SyncedField.SyncedString;
import pneumaticCraft.lib.Log;

public class NetworkUtils{

    /*private static Map<Class, List<Field>> extraMarkedFields = new HashMap<Class, List<Field>>();

    public static void addExtraMarkedField(Class clazz, Field field){
        List<Field> markedFields = extraMarkedFields.get(clazz);
        if(markedFields == null) {
            markedFields = new ArrayList<Field>();
            extraMarkedFields.put(clazz, markedFields);
        }
        markedFields.add(field);
    }*/

    public static List<SyncedField> getSyncedFields(Object te, Class searchedAnnotation){
        List<SyncedField> syncedFields = new ArrayList<SyncedField>();
        Class examinedClass = te.getClass();
        while(examinedClass != null) {
            for(Field field : examinedClass.getDeclaredFields()) {
                if(field.getAnnotation(searchedAnnotation) != null) {
                    syncedFields.addAll(getSyncedFieldsForField(field, te, searchedAnnotation));
                }
            }
            examinedClass = examinedClass.getSuperclass();
        }
        return syncedFields;
    }

    private static List<SyncedField> getSyncedFieldsForField(Field field, Object te, Class searchedAnnotation){
        boolean isLazy = field.getAnnotation(LazySynced.class) != null;
        List<SyncedField> syncedFields = new ArrayList<SyncedField>();
        SyncedField syncedField = getSyncedFieldForField(field, te);
        if(syncedField != null) {
            syncedFields.add(syncedField.setLazy(isLazy));
            return syncedFields;
        } else {
            Object o;
            try {
                int filteredIndex = field.getAnnotation(FilteredSynced.class) != null ? field.getAnnotation(FilteredSynced.class).index() : -1;
                field.setAccessible(true);
                o = field.get(te);
                if(o instanceof int[]) {
                    int[] array = (int[])o;
                    if(filteredIndex >= 0) {
                        syncedFields.add(new SyncedInt(te, field).setArrayIndex(filteredIndex).setLazy(isLazy));
                    } else {
                        for(int i = 0; i < array.length; i++) {
                            syncedFields.add(new SyncedInt(te, field).setArrayIndex(i).setLazy(isLazy));
                        }
                    }
                    return syncedFields;
                }
                if(o instanceof float[]) {
                    float[] array = (float[])o;
                    if(filteredIndex >= 0) {
                        syncedFields.add(new SyncedFloat(te, field).setArrayIndex(filteredIndex).setLazy(isLazy));
                    } else {
                        for(int i = 0; i < array.length; i++) {
                            syncedFields.add(new SyncedFloat(te, field).setArrayIndex(i).setLazy(isLazy));
                        }
                    }
                    return syncedFields;
                }
                if(o instanceof double[]) {
                    double[] array = (double[])o;
                    if(filteredIndex >= 0) {
                        syncedFields.add(new SyncedDouble(te, field).setArrayIndex(filteredIndex).setLazy(isLazy));
                    } else {
                        for(int i = 0; i < array.length; i++) {
                            syncedFields.add(new SyncedDouble(te, field).setArrayIndex(i).setLazy(isLazy));
                        }
                    }
                    return syncedFields;
                }
                if(o instanceof boolean[]) {
                    boolean[] array = (boolean[])o;
                    if(filteredIndex >= 0) {
                        syncedFields.add(new SyncedBoolean(te, field).setArrayIndex(filteredIndex).setLazy(isLazy));
                    } else {
                        for(int i = 0; i < array.length; i++) {
                            syncedFields.add(new SyncedBoolean(te, field).setArrayIndex(i).setLazy(isLazy));
                        }
                    }
                    return syncedFields;
                }
                if(o instanceof String[]) {
                    String[] array = (String[])o;
                    if(filteredIndex >= 0) {
                        syncedFields.add(new SyncedString(te, field).setArrayIndex(filteredIndex).setLazy(isLazy));
                    } else {
                        for(int i = 0; i < array.length; i++) {
                            syncedFields.add(new SyncedString(te, field).setArrayIndex(i).setLazy(isLazy));
                        }
                    }
                    return syncedFields;
                }
                if(o.getClass().isArray() && o.getClass().getComponentType().isEnum()) {
                    Object[] enumArray = (Object[])o;
                    if(filteredIndex >= 0) {
                        syncedFields.add(new SyncedEnum(te, field).setArrayIndex(filteredIndex).setLazy(isLazy));
                    } else {
                        for(int i = 0; i < enumArray.length; i++) {
                            syncedFields.add(new SyncedEnum(te, field).setArrayIndex(i).setLazy(isLazy));
                        }
                    }
                    return syncedFields;
                }
                if(o instanceof ItemStack[]) {
                    ItemStack[] array = (ItemStack[])o;
                    if(filteredIndex >= 0) {
                        syncedFields.add(new SyncedItemStack(te, field).setArrayIndex(filteredIndex).setLazy(isLazy));
                    } else {
                        for(int i = 0; i < array.length; i++) {
                            syncedFields.add(new SyncedItemStack(te, field).setArrayIndex(i).setLazy(isLazy));
                        }
                    }
                    return syncedFields;
                }
                if(o instanceof FluidTank[]) {
                    FluidTank[] array = (FluidTank[])o;
                    if(filteredIndex >= 0) {
                        syncedFields.add(new SyncedFluidTank(te, field).setArrayIndex(filteredIndex).setLazy(isLazy));
                    } else {
                        for(int i = 0; i < array.length; i++) {
                            syncedFields.add(new SyncedFluidTank(te, field).setArrayIndex(i).setLazy(isLazy));
                        }
                    }
                    return syncedFields;
                }
                if(field.getType().isArray()) {
                    Object[] array = (Object[])o;
                    for(Object obj : array) {
                        syncedFields.addAll(getSyncedFields(obj, searchedAnnotation));
                    }
                } else {
                    syncedFields.addAll(getSyncedFields(o, searchedAnnotation));
                }
                if(syncedFields.size() > 0) return syncedFields;
            } catch(Exception e) {
                e.printStackTrace();
            }
            Log.warning("Field " + field + " didn't produce any syncable fields!");
            return syncedFields;
        }
    }

    private static SyncedField getSyncedFieldForField(Field field, Object te){
        if(int.class.isAssignableFrom(field.getType())) return new SyncedInt(te, field);
        if(float.class.isAssignableFrom(field.getType())) return new SyncedFloat(te, field);
        if(double.class.isAssignableFrom(field.getType())) return new SyncedDouble(te, field);
        if(boolean.class.isAssignableFrom(field.getType())) return new SyncedBoolean(te, field);
        if(String.class.isAssignableFrom(field.getType())) return new SyncedString(te, field);
        if(field.getType().isEnum()) return new SyncedEnum(te, field);
        if(ItemStack.class.isAssignableFrom(field.getType())) return new SyncedItemStack(te, field);
        if(FluidTank.class.isAssignableFrom(field.getType())) return new SyncedFluidTank(te, field);
        return null;
    }
}
