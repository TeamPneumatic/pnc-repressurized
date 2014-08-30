package pneumaticCraft.common.block.tubes;

import net.minecraft.world.World;
import pneumaticCraft.api.tileentity.IPneumaticMachine;

public interface IPneumaticPosProvider extends IPneumaticMachine{
    public World world();

    public int x();

    public int y();

    public int z();
}
