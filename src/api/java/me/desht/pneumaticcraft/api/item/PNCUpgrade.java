package me.desht.pneumaticcraft.api.item;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents an upgrade which can be inserted into a PNC machine or item. Upgrades are Forge registry objects, and
 * should be registered in the usual way, using either {@link RegisterEvent} or (preferably)
 * {@link net.minecraftforge.registries.DeferredRegister}.
 * <p>
 * Upgrades can have multiple tiers; each upgrade <em>must</em> have one corresponding item registered per upgrade tier.
 * Such items <em>must</em> implement {@link IUpgradeItem}.
 */
public class PNCUpgrade {
    private static final AtomicInteger ids = new AtomicInteger();

    private final int maxTier;
    private final List<String> depModIds;
    private final int cacheId;  // for caching efficiency; may change between game restarts, don't use

    /**
     * Default constructor; register an upgrade with just one tier.
     */
    public PNCUpgrade() {
        this(1);
    }

    /**
     * Register an upgrade with just one tier.
     * @param maxTier maximum tier for this upgrade
     * @param depModIds zero or more mod IDs, at least one of which must be present for this upgrade to be relevant
     */
    public PNCUpgrade(int maxTier, String... depModIds) {
        this.maxTier = maxTier;
        this.depModIds = ImmutableList.copyOf(depModIds);
        this.cacheId = ids.getAndIncrement();
    }

    /**
     * A numeric id for the upgrade which is not guaranteed to be persistent across game restarts. Used internally
     * for performance; <strong>do not depend on the value of this</strong>.
     *
     * @return a numeric ID, for internal use
     */
    public int getCacheId() {
        return cacheId;
    }

    /**
     * Get the max upgrade tier allowable for this upgrade.
     *
     * @return the max tier
     */
    public final int getMaxTier() {
        return maxTier;
    }

    /**
     * Check if this upgrade's dependent mods are loaded. Used to control whether the upgrade is added to the creative
     * item list (and thus JEI), and whether any upgrade info is shown for it in GUI side tabs. Note that upgrades
     * are always registered in Forge registries, even if dependent mods are missing.
     *
     * @return true if this upgrade's dependencies are satisfied, false otherwise
     */
    public final boolean isDependencyLoaded() {
        return depModIds.isEmpty() || depModIds.stream().anyMatch(modid -> ModList.get().isLoaded(modid));
    }

    /**
     * Get the corresponding item for this upgrade and tier.
     *
     * @param tier the upgrade tier
     * @return a Minecraft item, or air if no corresponding item can be found
     * @throws NullPointerException if called before the upgrade is registered
     */
    public final Item getItem(int tier) {
        ResourceLocation rl = PneumaticRegistry.getInstance().getUpgradeRegistry().getItemRegistryName(this, tier);
        return tier > maxTier || rl == null ? Items.AIR : ForgeRegistries.ITEMS.getValue(rl);
    }

    /**
     * Get the corresponding item for this upgrade, assuming tier 1
     * @return a Minecraft item
     * @throws NullPointerException if called before the upgrade is registered
     */
    public final Item getItem() {
        return getItem(1);
    }

    /**
     * Get an itemstack for the given upgrade
     *
     * @return an upgrade itemstack, with a single item
     * @throws NullPointerException if called before the upgrade is registered
     */
    public final ItemStack getItemStack() {
        return getItemStack(1);
    }

    /**
     * Get an itemstack for the given upgrade
     *
     * @param count number of items in the stack
     * @return an upgrade itemstack
     * @throws NullPointerException if called before the upgrade is registered
     */
    public final ItemStack getItemStack(int count) {
        Item item = getItem();
        return item == null ? ItemStack.EMPTY : new ItemStack(item, count);
    }

    /**
     * Convenience method to get an upgrade from its corresponding item
     * @param stack the item
     * @return the upgrade, or null if the item isn't an upgrade
     */
    public static PNCUpgrade from(ItemStack stack) {
        return stack.getItem() instanceof IUpgradeItem u ? u.getUpgradeType() : null;
    }
}
