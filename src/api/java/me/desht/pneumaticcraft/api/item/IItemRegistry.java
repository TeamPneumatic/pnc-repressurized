/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.item;

import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * Get an instance of this with {@link me.desht.pneumaticcraft.api.PneumaticRegistry.IPneumaticCraftInterface#getItemRegistry()}
 */
public interface IItemRegistry {
    /**
     * Register a third-party class that can contain items.  This is intended for classes from other mods - if it's
     * your class, just make it implement {@link IInventoryItem} directly.
     *
     * @param handler instance of any class that implements {@link IInventoryItem}
     */
    void registerInventoryItem(IInventoryItem handler);

    /**
     * Register a magnet suppressor; an object which can prevent the Magnet Upgrade from pulling in (usually item)
     * entities.
     *
     * @param suppressor a suppressor object
     */
    void registerMagnetSuppressor(IMagnetSuppressor suppressor);

    /**
     * Convenience method to check if an item matches a given filter item. Note that the filtering item (the first
     * parameter) could be a Tag Filter, Classify Filter, some other instance of {@link IFilteringItem}, or an item
     * which has the {@link me.desht.pneumaticcraft.api.PNCCapabilities#ITEM_FILTERING} capability, so parameter
     * order is important; provide the filtering item <strong>first</strong>, and the item to check second.
     *
     * @param filterStack the item to check against
     * @param stack the item being checked
     * @param checkDurability true if item durability should be taken into account
     * @param checkNBT true if item NBT should be taken into account
     * @param checkModSimilarity true to just match by the two items' mod IDs
     * @return true if the item passes the filter test, false otherwise
     */
    boolean doesItemMatchFilter(@Nonnull ItemStack filterStack, @Nonnull ItemStack stack, boolean checkDurability, boolean checkNBT, boolean checkModSimilarity);

    /**
     * Register a handler to modify the effective volume of a pneumatic item (i.e. one that holds air/pressure).
     *
     * @param modifierFunc a volume modifier function
     */
    void registerPneumaticVolumeModifier(ItemVolumeModifier modifierFunc);

    /**
     * Get the modified volume for a given item, based on the volume modifiers registered with
     * {@link #registerPneumaticVolumeModifier(ItemVolumeModifier)}.
     *
     * @param stack the ItemStack to check
     * @param originalVolume the original volume, which is the item's base volume,
     *                       possibly already modified by Volume Upgrades
     * @return the modified volume
     */
    int getModifiedVolume(ItemStack stack, int originalVolume);

    /**
     * Get some information for the given Spawner Core item.
     *
     * @param stack an ItemStack, which must be a Spawner Core
     * @return a spawner core stats object, to query and manipulate the item
     * @throws IllegalArgumentException if the passed ItemStack is not a Spawner Core
     */
    ISpawnerCoreStats getSpawnerCoreStats(ItemStack stack);

    /**
     * Create an instance of PneumaticCraft's default item air handler implementation, suitable for registering an air
     * handler item capability.
     * <p>
     * You can use this method to register an air-handling capability for your items
     * via {@link net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent} and
     * {@link me.desht.pneumaticcraft.api.PNCCapabilities#AIR_HANDLER_ITEM}.
     *
     * @param stack the ItemStack, whose item must implement {@link me.desht.pneumaticcraft.api.pressure.IPressurizableItem}
     * @return an implementation of IAirHandler
     * @implNote this air handler stores the item's air amount in the {@code}pneumaticcraft:air{@code} integer NBT tag
     * @throws IllegalArgumentException if the stack's item does not implement {@link me.desht.pneumaticcraft.api.pressure.IPressurizableItem}
     */
    IAirHandlerItem makeItemAirHandler(ItemStack stack);

    /**
     * Register an item launch behaviour for use by the Air Cannon and Pneumatic Chestplate Item Launcher (Dispenser
     * upgrade). Call this from a {@link net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent} listener; no
     * {@code enqueueWork()} required.
     *
     * @param behaviour the launch behaviour to register
     */
    void registerItemLaunchBehaviour(ILaunchBehaviour behaviour);
}
