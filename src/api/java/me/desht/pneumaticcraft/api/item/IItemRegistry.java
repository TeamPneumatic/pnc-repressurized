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

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

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
     * Register an item or block as being able to accept PneumaticCraft upgrades. This is safe to call in your own
     * mod's init thread.
     *
     * @param upgradeAcceptor the upgrade acceptor
     * @deprecated no longer required and is now a no-op; superceded by {@link IUpgradeRegistry}
     */
    @Deprecated(forRemoval = true)
    void registerUpgradeAcceptor(IUpgradeAcceptor upgradeAcceptor);

    /**
     * Convenience method to create an Item implementing the {@link IUpgradeItem} interface, which can be used as a
     * PneumaticCraft upgrade. This item has the default PneumaticCraft tooltip behaviour in that
     * {@link #addTooltip(PNCUpgrade, List)} is called when Shift is held while hovering over the item.
     * <p>
     * You can use this method when registering upgrade items as an alternative to creating an Item which implements
     * {@code IUpgrad£eItem} yourself.
     *
     * @param upgrade a supplier for the upgrade object, which will not yet be registered
     * @param tier upgrade tier of this item
     * @return an item
     * @deprecated superceded by {@link IUpgradeRegistry#makeUpgradeItem(Supplier, int)}
     */
    @Deprecated(forRemoval = true)
    Item makeUpgradeItem(Supplier<PNCUpgrade> upgrade, int tier);

    /**
     * Can be used for custom upgrade items to handle tooltips. This will work for implementors registered via
     * {@link IItemRegistry#registerUpgradeAcceptor(IUpgradeAcceptor)}. This is intended to be called from
     * {@link net.minecraft.world.item.Item#appendHoverText(ItemStack, Level, List, TooltipFlag)} method to display
     * which machines and/or items accept it.
     *  @param upgrade the upgrade item
     * @param tooltip the tooltip string list to append to
     * @deprecated superceded by {@link IUpgradeRegistry#addUpgradeTooltip(PNCUpgrade, List)}
     */
    @Deprecated(forRemoval = true)
    void addTooltip(PNCUpgrade upgrade, List<Component> tooltip);

    /**
     * Register a magnet suppressor; an object which can prevent the Magnet Upgrade from pulling in (usually item)
     * entities.
     *
     * @param suppressor a suppressor object
     */
    void registerMagnetSuppressor(IMagnetSuppressor suppressor);

    /**
     * Convenience method to check if an item matches a given filter item. Note that the filtering item (the first
     * parameter) could be a Tag Filter, Classify Filter, or other instance of {@link IFilteringItem}, so parameter
     * order is important; provide the filtering item first, and the item to check second.
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
     * Create an instance of PneumaticCraft's default item air handler provider, suitable for returning
     * from {@link net.minecraft.world.item.Item#initCapabilities(ItemStack, CompoundTag)}.
     * <p>
     * You can use this method for your own air-handling items, <em>provided that</em> your item implements
     * {@link me.desht.pneumaticcraft.api.pressure.IPressurizableItem}. If you want to avoid a hard dependency on
     * PneumaticCraft, then create your own custom implementation of {@link IAirHandlerItem},
     * and attach that implementation to your item via {@link net.minecraftforge.event.AttachCapabilitiesEvent}.
     *
     * @param stack the ItemStack
     * @param maxPressure the maximum pressure allowed for the item
     * @return an implementation of IAirHandler
     * @implNote this air handler stores the item's air amount in the {@code}pneumaticcraft:air{@code} integer NBT tag
     */
    IAirHandlerItem.Provider makeItemAirHandlerProvider(ItemStack stack, float maxPressure);

    /**
     * Register an item launch behaviour for use by the Air Cannon and Pneumatic Chestplate Item Launcher (Dispenser
     * upgrade). Call this from a {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent} listener; no
     * {@code enqueueWork()} required.
     *
     * @param behaviour the launch behaviour to register
     */
    void registerItemLaunchBehaviour(ILaunchBehaviour behaviour);

    /**
     * Get the upgrade registry handler, which can be used to register custom upgrades with block entities, entities
     * and items.
     *
     * @return the upgrade registry
     * @deprecated use {@link PneumaticRegistry.IPneumaticCraftInterface#getUpgradeRegistry()}
     */
    @Deprecated(forRemoval = true)
    IUpgradeRegistry getUpgradeRegistry();

}
