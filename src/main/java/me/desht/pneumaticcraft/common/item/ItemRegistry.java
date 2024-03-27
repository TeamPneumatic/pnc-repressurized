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

package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.*;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import me.desht.pneumaticcraft.common.capabilities.AirHandlerItemStack;
import me.desht.pneumaticcraft.common.util.ItemLaunching;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public enum ItemRegistry implements IItemRegistry {
    INSTANCE;

    private final List<Item> inventoryItemBlacklist = new ArrayList<>();
    public final List<IInventoryItem> inventoryItems = new ArrayList<>();
    private final List<IMagnetSuppressor> magnetSuppressors = new ArrayList<>();
    private final List<ItemVolumeModifier> volumeModifiers = new ArrayList<>();

    public static ItemRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerInventoryItem(@Nonnull IInventoryItem handler) {
        Validate.notNull(handler, "handler must not be null!");
        inventoryItems.add(handler);
    }

    @Override
    public void registerMagnetSuppressor(IMagnetSuppressor suppressor) {
        magnetSuppressors.add(suppressor);
    }

    @Override
    public boolean doesItemMatchFilter(@Nonnull ItemStack filterStack, @Nonnull ItemStack stack, boolean checkDurability, boolean checkNBT, boolean checkModSimilarity) {
        return PneumaticCraftUtils.doesItemMatchFilter(filterStack, stack, checkDurability, checkNBT, checkModSimilarity);
    }

    @Override
    public void registerPneumaticVolumeModifier(ItemVolumeModifier modifierFunc) {
        volumeModifiers.add(modifierFunc);
    }

    @Override
    public ISpawnerCoreStats getSpawnerCoreStats(ItemStack stack) {
        Validate.isTrue(stack.getItem() instanceof SpawnerCoreItem, "item is not a Spawner Core!");
        return SpawnerCoreItem.SpawnerCoreStats.forItemStack(stack);
    }

    @Override
    public IAirHandlerItem makeItemAirHandler(ItemStack stack) {
        return new AirHandlerItemStack(stack);
    }

    @Override
    public void registerItemLaunchBehaviour(ILaunchBehaviour behaviour) {
        ItemLaunching.registerBehaviour(behaviour);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean shouldSuppressMagnet(Entity e) {
        return magnetSuppressors.stream().anyMatch(s -> s.shouldSuppressMagnet(e));
    }

    @Override
    public int getModifiedVolume(ItemStack stack, int originalVolume) {
        for (ItemVolumeModifier modifier : volumeModifiers) {
            originalVolume = modifier.getNewVolume(stack, originalVolume);
        }
        return originalVolume;
    }

    public void addVolumeModifierInfo(ItemStack stack, List<Component> text) {
        volumeModifiers.forEach(modifier -> modifier.addInfo(stack, text));
    }

    /**
     * Get a list of the items contained in the given item.  This uses the {@link IInventoryItem} interface.
     *
     * @param item the item to check
     * @return a list of the items contained within the given item
     */
    public List<ItemStack> getStacksInItem(@Nonnull ItemStack item) {
        List<ItemStack> items = new ArrayList<>();
        if (item.getItem() instanceof IInventoryItem && !inventoryItemBlacklist.contains(item.getItem())) {
            try {
                ((IInventoryItem) item.getItem()).getStacksInItem(item, items);
            } catch (Throwable e) {
                Log.error("An InventoryItem crashed:");
                e.printStackTrace();
                inventoryItemBlacklist.add(item.getItem());
            }
        } else {
            Iterator<IInventoryItem> iterator = getInstance().inventoryItems.iterator();
            while (iterator.hasNext()) {
                try {
                    iterator.next().getStacksInItem(item, items);
                } catch (Throwable e) {
                    Log.error("An InventoryItem crashed:");
                    e.printStackTrace();
                    iterator.remove();
                }
            }
        }
        return items;
    }
}
