package me.desht.pneumaticcraft.common.progwidgets;

import net.minecraft.util.math.BlockPos;

import java.util.Set;

public interface IAreaProvider {
    void getArea(Set<BlockPos> area);
}
