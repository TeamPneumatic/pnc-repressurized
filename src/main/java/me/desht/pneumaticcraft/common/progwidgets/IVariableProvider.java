package me.desht.pneumaticcraft.common.progwidgets;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public interface IVariableProvider{
    boolean hasCoordinate(String varName);
    BlockPos getCoordinate(String varName);

    boolean hasStack(String varName);
    @Nonnull
    ItemStack getStack(String varName);
}
