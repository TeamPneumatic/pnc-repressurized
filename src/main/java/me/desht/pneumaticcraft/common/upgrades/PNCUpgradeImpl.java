package me.desht.pneumaticcraft.common.upgrades;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PNCUpgradeImpl implements PNCUpgrade {
    private static final AtomicInteger ids = new AtomicInteger();

    private final ResourceLocation id;
    private final int maxTier;
    private final List<String> depModIds;
    private final int cacheId;  // for caching efficiency; may change between game restarts, don't use

    /**
     * Create an upgrade.
     * @param id a unique ID for this upgrade; this will be used to determine the corresponding item ID(s)
     * @param maxTier maximum tier for this upgrade
     * @param depModIds zero or more mod IDs, at least one of which must be present for this upgrade to be relevant
     */
    PNCUpgradeImpl(ResourceLocation id, int maxTier, String... depModIds) {
        this.id = id;
        this.maxTier = maxTier;
        this.depModIds = ImmutableList.copyOf(depModIds);
        this.cacheId = ids.getAndIncrement();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public int getCacheId() {
        return cacheId;
    }

    @Override
    public final int getMaxTier() {
        return maxTier;
    }

    @Override
    public final boolean isDependencyLoaded() {
        return depModIds.isEmpty() || depModIds.stream().anyMatch(modid -> ModList.get().isLoaded(modid));
    }

    @Override
    public final ResourceLocation getItemRegistryName(int tier) {
        Validate.isTrue(tier > 0 && tier <= maxTier, "tier must be in range 1 .. " + maxTier + "!");
        String path = id.getPath() + "_upgrade" + (maxTier > 1 ? "_" + tier : "");
        return new ResourceLocation(id.getNamespace(), path);
    }

    @Override
    public final Item getItem(int tier) {
        return BuiltInRegistries.ITEM.get(getItemRegistryName(tier));
    }

    @Override
    public final ItemStack getItemStack(int count) {
        Item item = getItem();
        if (item == null) return ItemStack.EMPTY;
        // interpreting count as the tier and assuming not more than one tiered upgrade allowed (true today but not necessarily forever...)
        return maxTier == 1 ? new ItemStack(item, count) : new ItemStack(getItem(count));
    }
}
