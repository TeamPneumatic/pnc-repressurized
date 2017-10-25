package me.desht.pneumaticcraft.common.thirdparty.openblocks;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import net.minecraftforge.fluids.FluidRegistry;

public class OpenBlocks implements IThirdParty {
    @Override
    public void init() {
        PneumaticRegistry.getInstance().registerXPLiquid(FluidRegistry.getFluid("xpjuice"), 20);
    }
}
