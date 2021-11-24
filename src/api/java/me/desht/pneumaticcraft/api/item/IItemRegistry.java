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
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

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
     * Register an item or block as being able to accept PneumaticCraft upgrades.
     *
     * @param upgradeAcceptor the upgrade acceptor
     */
    void registerUpgradeAcceptor(IUpgradeAcceptor upgradeAcceptor);

    /**
     * Can be used for custom upgrade items to handle tooltips. This will work for implementors registered via
     * {@link IItemRegistry#registerUpgradeAcceptor(IUpgradeAcceptor)}. This is intended to be called from
     * {@link Item#appendHoverText(ItemStack, World, List, ITooltipFlag)} method to display
     * which machines and/or items accept it.
     *
     * @param upgrade the upgrade item
     * @param tooltip the tooltip string list to append to
     */
    void addTooltip(EnumUpgrade upgrade, List<ITextComponent> tooltip);

    /**
     * Register a magnet suppressor; an object which can prevent the Magnet Upgrade from pulling in (usually item)
     * entities.
     *
     * @param suppressor a suppressor object
     */
    void registerMagnetSuppressor(IMagnetSuppressor suppressor);

    /**
     * Convenience method to check if an item matches a given filter item. Note that the filtering item (the first
     * parameter) could be a Tag Filter or other instance of {@link ITagFilteringItem}, so parameter order is important;
     * provide the filtering item first, and the item to check second.
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
     * from {@link Item#initCapabilities(ItemStack, CompoundNBT)}.
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
}
