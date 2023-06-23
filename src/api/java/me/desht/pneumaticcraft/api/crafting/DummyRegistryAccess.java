package me.desht.pneumaticcraft.api.crafting;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Dummy registry access which does nothing useful, but we can pass it to methods where we know it's not being used,
 * like {@link net.minecraft.world.item.crafting.ShapedRecipe#getResultItem(RegistryAccess)}.
 */
public enum DummyRegistryAccess implements RegistryAccess {
    INSTANCE;

    @Override
    public <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> pRegistryKey) {
        return Optional.empty();
    }

    @Override
    public Stream<RegistryEntry<?>> registries() {
        return Stream.empty();
    }
}
