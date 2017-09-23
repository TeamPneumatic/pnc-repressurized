package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Field;

public abstract class SyncedField<T> {
    private final Field field;
    private final Object te;
    private T lastValue;
    private int arrayIndex = -1;
    private boolean isLazy;

    public SyncedField(Object te, Field field) {
        this.field = field;
        field.setAccessible(true);
        this.te = te;
    }

    public SyncedField setArrayIndex(int arrayIndex) {
        this.arrayIndex = arrayIndex;
        return this;
    }

    public SyncedField setLazy(boolean lazy) {
        this.isLazy = lazy;
        return this;
    }

    public boolean update() {
        try {
            T value = arrayIndex >= 0 ? getValueForArray(field.get(te), arrayIndex) : retrieveValue(field, te);
            if (lastValue == null && value != null || lastValue != null && !equals(lastValue, value)) {
                lastValue = value == null ? null : copyWhenNecessary(value);
                return !isLazy;
            }
        } catch (Throwable e) {
            Log.error("A problem occured when trying to sync the field of " + te.toString() + ". Field: " + field.toString());
            e.printStackTrace();
        }
        return false;
    }

    protected boolean equals(T oldValue, T newValue) {
        return oldValue.equals(newValue);
    }

    protected T copyWhenNecessary(T oldValue) {
        return oldValue;
    }

    protected T retrieveValue(Field field, Object te) throws Exception {
        return (T) field.get(te);
    }

    protected void injectValue(Field field, Object te, T value) throws Exception {
        field.set(te, value);
    }

    protected abstract T getValueForArray(Object array, int index);

    protected abstract void setValueForArray(Object array, int index, T value) throws Exception;

    public T getValue() {
        return lastValue;
    }

    @SideOnly(Side.CLIENT)
    public void setValue(T value) {
        try {
            if (arrayIndex >= 0) {
                setValueForArray(field.get(te), arrayIndex, value);
            } else {
                injectValue(field, te, value);
            }
        } catch (Exception e) {
            Log.error("A problem occured when trying to sync the field of " + te.toString() + ". Field: " + field.toString());
            e.printStackTrace();
        }
    }

    public static class SyncedInt extends SyncedField<Integer> {

        public SyncedInt(Object te, Field field) {
            super(te, field);
        }

        @Override
        protected Integer getValueForArray(Object array, int index) {
            return ((int[]) array)[index];
        }

        @Override
        protected void setValueForArray(Object array, int index, Integer value) throws Exception {
            ((int[]) array)[index] = value;
        }

    }

    public static class SyncedFloat extends SyncedField<Float> {

        public SyncedFloat(Object te, Field field) {
            super(te, field);
        }

        @Override
        protected Float getValueForArray(Object array, int index) {
            return ((float[]) array)[index];
        }

        @Override
        protected void setValueForArray(Object array, int index, Float value) throws Exception {
            ((float[]) array)[index] = value;
        }

    }

    public static class SyncedDouble extends SyncedField<Double> {

        public SyncedDouble(Object te, Field field) {
            super(te, field);
        }

        @Override
        protected Double getValueForArray(Object array, int index) {
            return ((double[]) array)[index];
        }

        @Override
        protected void setValueForArray(Object array, int index, Double value) throws Exception {
            ((double[]) array)[index] = value;
        }

    }

    public static class SyncedBoolean extends SyncedField<Boolean> {

        public SyncedBoolean(Object te, Field field) {
            super(te, field);
        }

        @Override
        protected Boolean getValueForArray(Object array, int index) {
            return ((boolean[]) array)[index];
        }

        @Override
        protected void setValueForArray(Object array, int index, Boolean value) throws Exception {
            ((boolean[]) array)[index] = value;
        }

    }

    public static class SyncedString extends SyncedField<String> {

        public SyncedString(Object te, Field field) {
            super(te, field);
        }

        @Override
        protected String getValueForArray(Object array, int index) {
            return ((String[]) array)[index];
        }

        @Override
        protected void setValueForArray(Object array, int index, String value) throws Exception {
            ((String[]) array)[index] = value;
        }

    }

    public static class SyncedEnum extends SyncedField<Byte> {

        public SyncedEnum(Object te, Field field) {
            super(te, field);
        }

        @Override
        protected Byte getValueForArray(Object array, int index) {
            return ((byte[]) array)[index];
        }

        @Override
        protected void setValueForArray(Object array, int index, Byte value) throws Exception {
            ((byte[]) array)[index] = value;
        }

        @Override
        protected Byte retrieveValue(Field field, Object te) throws Exception {
            Object[] enumTypes = field.getType().getEnumConstants();
            return (byte) ArrayUtils.indexOf(enumTypes, field.get(te));
        }

        @Override
        protected void injectValue(Field field, Object te, Byte value) throws Exception {
            Object enumType = field.getType().getEnumConstants()[value];
            field.set(te, enumType);
        }

    }

    public static class SyncedItemStack extends SyncedField<ItemStack> {

        public SyncedItemStack(Object te, Field field) {
            super(te, field);
        }

        @Override
        protected ItemStack getValueForArray(Object array, int index) {
            return ((ItemStack[]) array)[index];
        }

        @Override
        protected void setValueForArray(Object array, int index, ItemStack value) throws Exception {
            ((ItemStack[]) array)[index] = value;
        }
    }

    public static class SyncedFluidTank extends SyncedField<FluidStack> {

        public SyncedFluidTank(Object te, Field field) {
            super(te, field);
        }

        @Override
        protected FluidStack getValueForArray(Object array, int index) {
            return ((FluidTank[]) array)[index].getFluid();
        }

        @Override
        protected void setValueForArray(Object array, int index, FluidStack value) throws Exception {
            ((FluidTank[]) array)[index].setFluid(value);
        }

        @Override
        protected FluidStack retrieveValue(Field field, Object te) throws Exception {
            FluidTank tank = (FluidTank) field.get(te);
            return tank.getFluid();
        }

        @Override
        protected void injectValue(Field field, Object te, FluidStack value) throws Exception {
            FluidTank tank = (FluidTank) field.get(te);
            tank.setFluid(value);
        }

        @Override
        protected boolean equals(FluidStack oldValue, FluidStack newValue) {
            return oldValue.isFluidEqual(newValue) && oldValue.amount == newValue.amount;
        }

        @Override
        protected FluidStack copyWhenNecessary(FluidStack oldValue) {
            return oldValue.copy();
        }
    }

    public static class SyncedItemStackHandler extends SyncedField<ItemStackHandler> {
        public SyncedItemStackHandler(Object te, Field field) {
            super(te, field);
        }

        @Override
        protected ItemStackHandler getValueForArray(Object array, int index) {
            return ((ItemStackHandler[]) array)[index];
        }

        @Override
        protected void setValueForArray(Object array, int index, ItemStackHandler value) throws Exception {
            ((ItemStackHandler[]) array)[index] = value;
        }

        @Override
        protected ItemStackHandler retrieveValue(Field field, Object te) throws Exception {
            ItemStackHandler handler = (ItemStackHandler) field.get(te);
            ItemStackHandler result = new ItemStackHandler(handler.getSlots());
            for (int i = 0; i < handler.getSlots(); i++) {
                result.setStackInSlot(i, handler.getStackInSlot(i));
            }
            return result;
        }

        @Override
        protected void injectValue(Field field, Object te, ItemStackHandler value) throws Exception {
            ItemStackHandler handler = (ItemStackHandler) field.get(te);
            for (int i = 0; i < value.getSlots(); i++) {
                handler.setStackInSlot(i, value.getStackInSlot(i));
            }
        }

        @Override
        protected boolean equals(ItemStackHandler oldValue, ItemStackHandler newValue) {
            if (oldValue.getSlots() != newValue.getSlots()) return false;
            for (int i = 0; i < oldValue.getSlots(); i++) {
                if (!ItemStack.areItemsEqual(oldValue.getStackInSlot(i), newValue.getStackInSlot(i))) {
                    return false;
                }
            }
            return true;
        }

        @Override
        protected ItemStackHandler copyWhenNecessary(ItemStackHandler oldValue) {
            ItemStackHandler result = new ItemStackHandler(oldValue.getSlots());
            for (int i = 0; i < oldValue.getSlots(); i++) {
                result.setStackInSlot(i, oldValue.getStackInSlot(i));
            }
            return result;
        }
    }
}
