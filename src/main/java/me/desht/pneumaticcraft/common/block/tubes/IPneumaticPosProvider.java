package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IPneumaticPosProvider extends IPneumaticMachine {
    World world();

    BlockPos pos();
}
