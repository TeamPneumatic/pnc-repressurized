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

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Locale;

public enum EnumUpgrade {
    VOLUME("volume"),
    DISPENSER("dispenser"),
    ITEM_LIFE("itemLife"),
    ENTITY_TRACKER("entityTracker"),
    BLOCK_TRACKER("blockTracker"),
    SPEED("speed"),
    SEARCH("search"),
    COORDINATE_TRACKER("coordinateTracker"),
    RANGE("range"),
    SECURITY("security"),
    MAGNET("magnet"),
    THAUMCRAFT("thaumcraft", 1, "thaumcraft"), /*Only around when Thaumcraft is */
    CHARGING("charging"),
    ARMOR("armor"),
    JET_BOOTS("jetboots", 5),
    NIGHT_VISION("night_vision"),
    SCUBA("scuba"),
    CREATIVE("creative"),
    AIR_CONDITIONING("air_conditioning", 1,"toughasnails"),
    INVENTORY("inventory"),
    JUMPING("jumping", 4),
    FLIPPERS("flippers"),
    STANDBY("standby"),
    MINIGUN("minigun"),
    RADIATION_SHIELDING("radiation_shielding", 1, "mekanism");

    private final String name;
    private final int maxTier;
    private final List<String> depModIds;

    EnumUpgrade(String name) {
        this(name, 1);
    }

    EnumUpgrade(String name, int maxTier, String... depModIds) {
        this.name = name;
        this.maxTier = maxTier;
        this.depModIds = ImmutableList.copyOf(depModIds);
    }

    public String getName() {
        return name;
    }

    public int getMaxTier() {
        return maxTier;
    }

    /**
     * Check if any of this upgrade's dependent mods are loaded.  If this returns false, then
     * {@link #getItem()} will return null.
     *
     * @return true if any of this upgrade's dependent mods are loaded, false otherwise
     */
    public boolean isDepLoaded() {
        return depModIds.isEmpty() || depModIds.stream().anyMatch(modid -> ModList.get().isLoaded(modid));
    }

    public Item getItem(int tier) {
        return tier > maxTier ? Items.AIR : ForgeRegistries.ITEMS.getValue(PneumaticRegistry.RL(getItemName(tier)));
    }

    public Item getItem() {
        return getItem(1);
    }

    public ItemStack getItemStack() {
        return getItemStack(1);
    }

    public ItemStack getItemStack(int count) {
        Item item = getItem();
        return item == null ? ItemStack.EMPTY : new ItemStack(getItem(), count);
    }

    public String getItemName(int tier) {
        String name = this.toString().toLowerCase(Locale.ROOT) + "_upgrade";
        return maxTier > 1 ? name + "_" + tier : name;
    }

    public static EnumUpgrade from(ItemStack stack) {
        return stack.getItem() instanceof IUpgradeItem ? ((IUpgradeItem) stack.getItem()).getUpgradeType() : null;
    }
}
