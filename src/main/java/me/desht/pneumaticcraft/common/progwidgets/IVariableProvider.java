package me.desht.pneumaticcraft.common.progwidgets;

import net.minecraft.util.math.BlockPos;

public interface IVariableProvider{
    public BlockPos getCoordinate(String varName);
}
