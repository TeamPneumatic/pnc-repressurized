package me.desht.pneumaticcraft.api.item;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents an upgrade which can be inserted into a PNC machine or item. Upgrades are Forge registry objects, and
 * should be registered in the usual way, using either {@link RegistryEvent.Register} or
 * {@link net.minecraftforge.registries.DeferredRegister}.
 * <p>
 * Upgrades can have multiple tiers; each upgrade <em>must</em> have one corresponding item registered per upgrade tier.
 * Such items <em>must</em> implement {@link IUpgradeItem}.
 */
public class PNCUpgrade extends ForgeRegistryEntry<PNCUpgrade> {
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
     * Get the registry name for the corresponding item for this upgrade, given a tier. Do not use this before the
     * upgrade itself has been registered!
     * <p>
     * The default naming strategy is to take the upgrade's registry name and simply append "_upgrade" to it (along with
     * the tier number if it's a multitier upgrade). You can override this strategy by extending this class and
     * overriding this method if you need to.
     *
     * @param tier tier of this upgrade
     * @return an item registry name
     * @throws NullPointerException if called before the upgrade is registered
     */
    protected ResourceLocation getItemRegistryName(int tier) {
        String registryName = Objects.requireNonNull(getRegistryName()).toString() + "_upgrade";
        if (getMaxTier() > 1) registryName += "_" + tier;
        return new ResourceLocation(registryName);
    }

    /**
     * Get the corresponding item for this upgrade and tier.
     *
     * @param tier the upgrade tier
     * @return a Minecraft item
     * @throws NullPointerException if called before the upgrade is registered
     */
    public final Item getItem(int tier) {
        return tier > maxTier ? Items.AIR : ForgeRegistries.ITEMS.getValue(getItemRegistryName(tier));
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
        if (item == null) return ItemStack.EMPTY;
        // FIXME 1.19 this doesn't handle the possibility of multiple tiered upgrades (which don't currently exist in PNC but might one day)
        if (maxTier == 1) {
            return new ItemStack(item, count);
        } else {
            // interpreting count as the tier and assuming not more than one tiered upgrade allowed (true today but not necessarily forever...)
            return new ItemStack(getItem(count));
        }
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
