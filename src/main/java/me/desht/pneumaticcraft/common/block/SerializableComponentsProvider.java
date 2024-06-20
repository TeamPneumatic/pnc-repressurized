package me.desht.pneumaticcraft.common.block;

import net.minecraft.core.component.DataComponentType;

import java.util.List;

/**
 * Used to gather serializable components for block loot table generation.
 */
@FunctionalInterface
public interface SerializableComponentsProvider {
    void addSerializableComponents(List<DataComponentType<?>> list);
}
