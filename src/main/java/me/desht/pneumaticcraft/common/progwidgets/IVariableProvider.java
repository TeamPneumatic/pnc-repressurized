package me.desht.pneumaticcraft.common.progwidgets;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.UUID;

public interface IVariableProvider{
    Optional<BlockPos> getCoordinate(UUID id, String varName);

    @Nonnull
    ItemStack getStack(UUID id, String varName);
}
