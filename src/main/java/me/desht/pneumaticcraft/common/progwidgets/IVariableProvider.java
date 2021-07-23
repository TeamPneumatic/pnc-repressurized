package me.desht.pneumaticcraft.common.progwidgets;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface IVariableProvider{
    boolean hasCoordinate(UUID id, String varName);
    BlockPos getCoordinate(UUID id, String varName);

    boolean hasStack(UUID id, String varName);
    @Nonnull
    ItemStack getStack(UUID id, String varName);
}
