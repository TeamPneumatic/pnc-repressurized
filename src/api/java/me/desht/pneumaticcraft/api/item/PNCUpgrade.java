package me.desht.pneumaticcraft.api.item;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents an upgrade which can be inserted into a PNC machine or item. Upgrades are Forge registry objects, and
 * should be registered in the usual way ({@code DeferredRegister&lt;PNCUpgrade&gt;} and/or
 * {@code RegistryEvent.Register&lt;PNCUpgrade&gt;}.
 * <p>
 * Upgrades can have multiple tiers; each upgrade must have one corresponding item registered per upgrade tier.
 * Such items must implement {@link IUpgradeItem}.
 */
public class PNCUpgrade extends ForgeRegistryEntry<PNCUpgrade> {
    private static final AtomicInteger ids = new AtomicInteger();

    private final ResourceLocation name;
    private final int maxTier;
    private final List<String> depModIds;
    private final int cacheId;  // for caching efficiency; may change between game restarts, don't use

    public PNCUpgrade(ResourceLocation name) {
        this(name, 1);
    }

    public PNCUpgrade(ResourceLocation name, int maxTier, String... depModIds) {
        this.name = name;
        this.maxTier = maxTier;
        this.depModIds = ImmutableList.copyOf(depModIds);
        this.cacheId = ids.getAndIncrement();
    }

    public final ResourceLocation getName() {
        return name;
    }

    public int getCacheId() {
        return cacheId;
    }

    public final int getMaxTier() {
        return maxTier;
    }

    public final boolean isDependencyLoaded() {
        return depModIds.isEmpty() || depModIds.stream().anyMatch(modid -> ModList.get().isLoaded(modid));
    }

    public ResourceLocation getItemRegistryName(int tier) {
        String registryName = getName().toString() + "_upgrade";
        if (getMaxTier() > 1) registryName += "_" + tier;
        return new ResourceLocation(registryName);
    }

    public final Item getItem(int tier) {
        return tier > maxTier ? Items.AIR : ForgeRegistries.ITEMS.getValue(getItemRegistryName(tier));
    }

    public final Item getItem() {
        return getItem(1);
    }

    public final ItemStack getItemStack() {
        return getItemStack(1);
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PNCUpgrade upgrade)) return false;
        return name.equals(upgrade.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
