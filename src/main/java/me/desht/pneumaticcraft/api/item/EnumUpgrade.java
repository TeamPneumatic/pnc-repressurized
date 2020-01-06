package me.desht.pneumaticcraft.api.item;

import me.desht.pneumaticcraft.common.item.ItemMachineUpgrade;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

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
    FLIPPERS("flippers");

    private final String name;
    private final int maxTier;
    private final String depModId;

    EnumUpgrade(String name) {
        this(name, 1,null);
    }

    EnumUpgrade(String name, int maxTier) {
        this(name, maxTier, null);
    }

    EnumUpgrade(String name, int maxTier, String depModId) {
        this.name = name;
        this.maxTier = maxTier;
        this.depModId = depModId;
    }

    public String getName() {
        return name;
    }

    public int getMaxTier() {
        return maxTier;
    }

    /**
     * Check if this upgrade's dependent mod (if any) is loaded.  If this returns false, then
     * {@link #getItem()} will return null.
     *
     * @return true if this upgrade's dependent mod is loaded, false otherwise
     */
    public boolean isDepLoaded() {
        return depModId == null || ModList.get().isLoaded(depModId);
    }

    public Item getItem(int tier) {
        if (!isDepLoaded() || tier > maxTier) return null;
        return ForgeRegistries.ITEMS.getValue(RL(getItemName(tier)));
//        return isDepLoaded() ? ModItems.Registration.UPGRADES.get(this) : null;
    }

    public Item getItem() {
        return getItem(1);
    }

    public ItemStack getItemStack() {
        Item item = getItem();
        return item == null ? ItemStack.EMPTY : new ItemStack(getItem());
    }

    public String getItemName(int tier) {
        String name = this.toString().toLowerCase() + "_upgrade";
        if (maxTier > 1) name = name + "_" + tier;
        return name;
    }

    public static EnumUpgrade from(ItemStack stack) {
        return stack.getItem() instanceof ItemMachineUpgrade ? ((ItemMachineUpgrade) stack.getItem()).getUpgradeType() : null;
    }
}
