package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.util.ResourceLocation;

public class ModuleSafetyValve extends TubeModuleRedstoneReceiving {

    @Override
    public void update() {
        super.update();
        if (!pressureTube.world().isRemote) {
            if (pressureTube.getAirHandler(null).getPressure() > getThreshold()) {
                pressureTube.getAirHandler(null).airLeak(dir);
            }
        }
    }

    @Override
    public ResourceLocation getType() {
        return Names.MODULE_SAFETY_VALVE;
    }

}
