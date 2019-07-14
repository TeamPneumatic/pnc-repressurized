package me.desht.pneumaticcraft.api.item;

import net.minecraft.item.ItemStack;

/**
 * Implement this interface on items or entities which support the concept of pressure. Any item implementing this
 * interface will be able to (dis)charge in a Charging Station.
 * <p>
 * Don't use this for tile entities - see instead {@link me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine}
 */
public interface IPressurizable {
    /**
     * This method should return the current pressure of the ItemStack given.
     *
     * @param iStack the item stack to check; could be empty if the pressurizable is an entity
     * @return pressure in bar
     */
    float getPressure(ItemStack iStack);

    /**
     * This method is used to charge or discharge a pneumatic item. When the
     * value is negative the item is discharging.
     *
     * @param iStack the item stack to be (dis)charged; could be empty if the pressurizable is an entity
     * @param amount amount in mL that the item/entity is (dis)charging
     */
    void addAir(ItemStack iStack, int amount);

    /**
     * This method should return the maximum pressure of a pneumatic item. If it has reached this maximum, it won't
     * explode, but it won't (try to) charge either.
     *
     * @param iStack the item stack to check; could be empty if the pressurizable is an entity
     * @return maximum pressure in bar.
     */
    float maxPressure(ItemStack iStack);

    /**
     * Get the volume for this item, i.e. the amount of air stored at a pressure
     * of 1 bar.  It follows that the current air stored in an item is
     * {@code getPressure(stack) * getVolume(stack)}, and the maximum air storage
     * is {@code getMaxPressure(stack) * getVolume(stack)}.
     *
     * @param iStack the item stack to check; could be empty if the pressurizable is an entity
     * @return the item's air volume
     */
    int getVolume(ItemStack iStack);

    /**
     * Get the pressurizable object represented by the given itemstack, if any.
     *
     * @param stack the stack to check
     * @return the pressurizable object, or null if the itemstack isn't pressurizable
     */
    static IPressurizable of(ItemStack stack) {
        return stack.getItem() instanceof IPressurizable ? (IPressurizable) stack.getItem() : null;
    }
}
